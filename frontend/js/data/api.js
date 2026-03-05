import { loadDB, saveDB, uid, nowISO } from "./storage.js";
import { sha256 } from "./crypto.js";
import { makeSeed } from "./seed.js";

export function ensureDB(){
  let db = loadDB();
  if(!db){
    db = makeSeed();
    saveDB(db);
  }
  return db;
}

export function getSession(){
  try { return JSON.parse(localStorage.getItem("bellator_session_v1") || "null"); }
  catch { return null; }
}
export function setSession(sess){
  localStorage.setItem("bellator_session_v1", JSON.stringify(sess));
}
export function clearSession(){
  localStorage.removeItem("bellator_session_v1");
}

export async function login(email, senha){
  const db = ensureDB();
  const pass = await sha256(senha);
  const user = db.users.find(u => u.email.toLowerCase() === email.toLowerCase() && u.senhaHash === pass);
  if(!user) throw new Error("E-mail ou senha inválidos.");
  const sess = { userId: user.id, role: user.role, at: nowISO() };
  setSession(sess);
  return { user, sess };
}

export async function register({nome, email, telefone, senha}){
  const db = ensureDB();
  const exists = db.users.some(u => u.email.toLowerCase() === email.toLowerCase());
  if(exists) throw new Error("Esse e-mail já está cadastrado.");
  const user = {
    id: uid("cli"),
    role: "cliente",
    nome: nome.trim(),
    email: email.trim(),
    telefone: telefone.trim(),
    senhaHash: await sha256(senha),
    createdAt: nowISO()
  };
  db.users.push(user);
  saveDB(db);
  const sess = { userId: user.id, role: user.role, at: nowISO() };
  setSession(sess);
  return { user, sess };
}

export function me(){
  const db = ensureDB();
  const sess = getSession();
  if(!sess) return null;
  const user = db.users.find(u => u.id === sess.userId);
  if(!user) return null;
  return { user, sess };
}

export function logout(){ clearSession(); }

export function listServices(){ return ensureDB().services; }
export function listBarbers(){ return ensureDB().barbers; }

export function listAppointmentsForUser(user){
  const db = ensureDB();
  if(user.role === "cliente"){
    return db.appointments.filter(a => a.clienteId === user.id).sort((a,b)=> b.dataHora.localeCompare(a.dataHora));
  }
  if(user.role === "barbeiro"){
    const barberId = user.barberId;
    return db.appointments.filter(a => a.barbeiroId === barberId).sort((a,b)=> a.dataHora.localeCompare(b.dataHora));
  }
  // admin: all
  return db.appointments.slice().sort((a,b)=> b.dataHora.localeCompare(a.dataHora));
}

export function getAppointment(id){
  const db = ensureDB();
  return db.appointments.find(a => a.id === id);
}

export function cancelAppointment(id, byUserId){
  const db = ensureDB();
  const ap = db.appointments.find(a => a.id === id);
  if(!ap) throw new Error("Agendamento não encontrado.");
  ap.status = "Cancelado";
  ap.canceledAt = nowISO();
  ap.canceledBy = byUserId;
  saveDB(db);
  return ap;
}

export function completeAppointment(id, byUserId){
  const db = ensureDB();
  const ap = db.appointments.find(a => a.id === id);
  if(!ap) throw new Error("Agendamento não encontrado.");
  ap.status = "Concluído";
  ap.completedAt = nowISO();
  ap.completedBy = byUserId;
  saveDB(db);
  return ap;
}

export function createAppointment({clienteId, barbeiroId, servicoId, dataHoraISO}){
  const db = ensureDB();
  // enforce unique barber+datetime (similar to uq_barbeiro_horario)
  const clash = db.appointments.some(a => a.barbeiroId === barbeiroId && a.dataHora === dataHoraISO && a.status !== "Cancelado");
  if(clash) throw new Error("Esse horário acabou de ser ocupado. Escolha outro.");
  const ap = {
    id: uid("ag"),
    clienteId, barbeiroId, servicoId,
    dataHora: dataHoraISO,
    status: "Agendado",
    createdAt: nowISO()
  };
  db.appointments.push(ap);
  saveDB(db);
  return ap;
}

export function addReview({clienteId, agendamentoId, nota, comentario}){
  const db = ensureDB();
  db.reviews.push({ id: uid("rev"), clienteId, agendamentoId, nota, comentario, createdAt: nowISO() });
  saveDB(db);
}


// --- Helpers para telas de horário/agendas (modo localStorage demo) ---
// No modo "offline/local", não existe backend para sincronizar.
// Essas funções existem para manter compatibilidade com as páginas.
export async function refreshAppointmentsForUser(_user){
  // noop: dados já estão no localStorage
  return true;
}

export async function refreshAdminReport(){
  // noop: relatório é calculado localmente em adminReport()
  return true;
}

/**
 * Retorna uma lista de horários ocupados (HH:MM) de um barbeiro em uma data (YYYY-MM-DD).
 * Regra: considera agendamentos não cancelados e bloqueia todos os slots cobertos pela duração do serviço.
 * Ex: um serviço de 60min com step 30 bloqueia 2 slots (start e start+30).
 */
export async function getBusyTimes(barbeiroId, dateStr){
  const db = ensureDB();
  const barber = db.barbers.find(b => b.id === barbeiroId);
  const step = barber?.horarios?.stepMin || 30;

  const serviceMap = Object.fromEntries(db.services.map(s => [s.id, s]));
  const busy = new Set();

  for(const ap of db.appointments){
    if(ap.barbeiroId !== barbeiroId) continue;
    if(ap.status === "Cancelado") continue;

    const d = new Date(ap.dataHora);
    const localDateStr = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}-${String(d.getDate()).padStart(2,"0")}`;
    if(localDateStr !== dateStr) continue;

    const dur = serviceMap[ap.servicoId]?.duracaoMin || 30;
    for(let m=0; m < dur; m += step){
      const t = new Date(d.getTime() + m*60000);
      const key = `${String(t.getHours()).padStart(2,"0")}:${String(t.getMinutes()).padStart(2,"0")}`;
      busy.add(key);
    }
  }

  return Array.from(busy).sort();
}

export function adminReport(){
  const db = ensureDB();
  // faturamento: soma de atendimentos concluídos
  const mapService = Object.fromEntries(db.services.map(s=>[s.id,s]));
  const done = db.appointments.filter(a=>a.status==="Concluído");
  const total = done.reduce((sum,a)=> sum + (mapService[a.servicoId]?.preco || 0), 0);
  const byService = {};
  for(const a of done){
    const s = mapService[a.servicoId];
    if(!s) continue;
    byService[s.nome] = (byService[s.nome]||0) + 1;
  }
  return { total, doneCount: done.length, byService };
}