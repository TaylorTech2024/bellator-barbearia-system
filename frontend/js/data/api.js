import { nowISO } from "./storage.js";
import { makeSeed } from "./seed.js";

/**
 * API REMOTA (Spring Boot) mantendo a mesma "API" do frontend V5.
 * - session: bellator_session_v1
 * - cache: bellator_cache_v1
 */

const SESSION_KEY = "bellator_session_v1";
const CACHE_KEY = "bellator_cache_v1";

// default baseURL (pode sobrescrever em localStorage com bellator_api_base)
const BASE_KEY = "bellator_api_base";
export function getBaseUrl(){
  return (localStorage.getItem(BASE_KEY) || "http://localhost:8080").replace(/\/$/,'');
}
export function setBaseUrl(url){
  localStorage.setItem(BASE_KEY, String(url||"").trim().replace(/\/$/,''));
}

let __cache = loadCache();

function loadCache(){
  try {
    return JSON.parse(localStorage.getItem(CACHE_KEY) || "null") || {
      services: [],
      barbers: [],
      appointments: [],
      adminReport: { total:0, doneCount:0, byService:{} },
      users: [] // usado só como compat (não preenche no modo remoto)
    };
  } catch {
    return { services: [], barbers: [], appointments: [], adminReport: { total:0, doneCount:0, byService:{} }, users: [] };
  }
}
function saveCache(){
  localStorage.setItem(CACHE_KEY, JSON.stringify(__cache));
}

export function ensureDB(){
  // compat: páginas antigas esperam db.services / db.barbers / db.users / db.appointments
  return __cache;
}

export async function bootstrap(){
  // garante cache e tenta puxar catálogo (serviços + barbeiros)
  __cache = loadCache();
  await refreshCatalog();
}

function statusLabel(status){
  if(status === "AGENDADO") return "Agendado";
  if(status === "CONCLUIDO") return "Concluído";
  if(status === "CANCELADO") return "Cancelado";
  // fallback
  return status || "-";
}

function toLegacyAppointment(a){
  // backend pode retornar nested (admin) ou flat (me/barbeiro)
  // formato esperado no front:
  // { id, clienteId, barbeiroId, servicoId, dataHora, status, clienteNome? }
  const clienteId = a.cliente?.id ?? a.clienteId ?? null;
  const barbeiroId = a.barbeiro?.id ?? a.barbeiroId ?? null;
  const servicoId = a.servico?.id ?? a.servicoId ?? null;

  const data = a.data ?? a.dataStr ?? a.dataISO ?? null;
  const horario = a.horario ?? a.horarioStr ?? a.horarioISO ?? null;

  const dataHora = (data && horario) ? `${data}T${horario}` : (a.dataHora || a.dataHoraISO || null);

  return {
    id: a.id,
    clienteId,
    barbeiroId,
    servicoId,
    dataHora,
    status: statusLabel(a.status),
    clienteNome: a.clienteNome ?? a.cliente?.nome ?? "",
  };
}

async function http(path, options={}){
  const base = getBaseUrl();
  const url = base + path;
  const headers = options.headers ? {...options.headers} : {};
  headers["Content-Type"] = headers["Content-Type"] || "application/json";

  const sess = getSession();
  if(sess?.token) headers["Authorization"] = "Bearer " + sess.token;

  const res = await fetch(url, { ...options, headers });
  const isJson = (res.headers.get("content-type") || "").includes("application/json");
  const data = isJson ? await res.json().catch(()=>null) : await res.text().catch(()=>null);

  if(!res.ok){
    const msg = (data && (data.message || data.error || data.details)) ? (data.message || data.error || data.details) : "Erro na API";
    throw new Error(msg);
  }
  return data;
}

export function getSession(){
  try { return JSON.parse(localStorage.getItem(SESSION_KEY) || "null"); }
  catch { return null; }
}
export function setSession(sess){
  localStorage.setItem(SESSION_KEY, JSON.stringify(sess));
}
export function clearSession(){
  localStorage.removeItem(SESSION_KEY);
}

export async function login(email, senha){
  const out = await http("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, senha })
  });

  const role = String(out.role || "").toLowerCase(); // cliente/barbeiro/admin
  const user = {
    id: out.user?.id,
    nome: out.user?.nome,
    email: out.user?.email,
    role
  };
  const sess = { token: out.token, userId: user.id, role: user.role, at: nowISO() };
  setSession(sess);

  // mantém compat com ctx.me()
  __cache.__me = { user, sess };
  saveCache();

  return { user, sess };
}

export async function register({nome, email, telefone, senha}){
  // telefone não existe no backend; ignoramos no payload
  const out = await http("/auth/register", {
    method: "POST",
    body: JSON.stringify({ nome, email, senha, role: "CLIENTE" })
  });

  const role = String(out.role || "").toLowerCase();
  const user = {
    id: out.user?.id,
    nome: out.user?.nome,
    email: out.user?.email,
    role
  };
  const sess = { token: out.token, userId: user.id, role: user.role, at: nowISO() };
  setSession(sess);
  __cache.__me = { user, sess };
  saveCache();
  return { user, sess };
}

export function me(){
  const sess = getSession();
  if(!sess?.token) return null;

  // se já temos o __me cacheado, usa
  if(__cache.__me?.sess?.token === sess.token) return __cache.__me;

  // fallback: monta um "me" mínimo (nome/email não estarão aqui)
  return { user: { id: sess.userId, role: sess.role, nome: "Usuário" }, sess };
}

export function logout(){
  clearSession();
  __cache.appointments = [];
  __cache.adminReport = { total:0, doneCount:0, byService:{} };
  delete __cache.__me;
  saveCache();
}

export async function refreshCatalog(){
  try {
    const [services, barbers] = await Promise.all([
      http("/servicos", { method:"GET" }),
      http("/barbeiros", { method:"GET" })
    ]);
    __cache.services = (services||[]).map(s => ({
      id: s.id,
      nome: s.nome,
      preco: s.preco,
      duracaoMinutos: s.duracaoMinutos
    }));

    __cache.barbers = (barbers||[]).map(b => ({
      id: b.id,
      nome: b.nome,
      especialidade: "Barbeiro",
      // email não é usado no UI, mas guardamos se vier
      email: b.email
    }));

    saveCache();
  } catch (e) {
    // fallback seed (mantém o app funcionando mesmo sem API)
    const seed = makeSeed();
    __cache.services = seed.services;
    __cache.barbers = seed.barbers;
    saveCache();
  }
}

export function listServices(){ return ensureDB().services; }
export function listBarbers(){ return ensureDB().barbers; }

export async function refreshAppointmentsForUser(user){
  if(!user) return [];
  try {
    let out = [];
    if(user.role === "cliente"){
      out = await http("/agendamentos/me", { method:"GET" });
    } else if(user.role === "barbeiro"){
      // agenda do dia (hoje)
      const today = new Date();
      const yyyy = today.getFullYear();
      const mm = String(today.getMonth()+1).padStart(2,"0");
      const dd = String(today.getDate()).padStart(2,"0");
      const d = `${yyyy}-${mm}-${dd}`;
      out = await http(`/agendamentos/barbeiro?data=${d}`, { method:"GET" });
    } else {
      // admin: lista todos (precisa ROLE_ADMIN)
      out = await http("/admin/agendamentos", { method:"GET" });
    }
export async function getBusyTimes(barbeiroId, dateStr){
  // dateStr: YYYY-MM-DD
  try{
    const out = await http(`/agenda?barbeiroId=${barbeiroId}&data=${dateStr}`, { method:"GET" });
    return Array.isArray(out) ? out : [];
  }catch(e){
    return [];
  }
}

    __cache.appointments = (out||[]).map(toLegacyAppointment);
    saveCache();
    return __cache.appointments;
  } catch (e) {
    // se falhar, mantém o que tinha
    return __cache.appointments || [];
  }
}

export function listAppointmentsForUser(user){
  // mantém compat (sync) usando cache
  const aps = __cache.appointments || [];
  if(!user) return [];
  if(user.role === "cliente") return aps.filter(a=>a.clienteId === user.id).sort((a,b)=> String(b.dataHora||"").localeCompare(String(a.dataHora||"")));
  if(user.role === "barbeiro") return aps.filter(a=>a.barbeiroId === user.id).sort((a,b)=> String(a.dataHora||"").localeCompare(String(b.dataHora||"")));
  return aps.slice().sort((a,b)=> String(b.dataHora||"").localeCompare(String(a.dataHora||"")));
}

export function getAppointment(id){
  return (__cache.appointments||[]).find(a => String(a.id) === String(id)) || null;
}

export async function cancelAppointment(id){
  const out = await http(`/agendamentos/${id}/cancelar`, { method:"PUT" });
  const mapped = toLegacyAppointment(out);
  __cache.appointments = (__cache.appointments||[]).map(a=> String(a.id)===String(id) ? mapped : a);
  saveCache();
  return mapped;
}

export async function completeAppointment(id){
  const out = await http(`/agendamentos/${id}/concluir`, { method:"PUT" });
  const mapped = toLegacyAppointment(out);
  __cache.appointments = (__cache.appointments||[]).map(a=> String(a.id)===String(id) ? mapped : a);
  saveCache();
  return mapped;
}

export async function createAppointment({ clienteId, barbeiroId, servicoId, dataHoraISO }){
  // compat: recebe dataHoraISO (Date.toISOString), envia para API separando data + horario
  const dt = new Date(dataHoraISO);
  if(Number.isNaN(dt.getTime())) throw new Error("Data/Horário inválido.");

  const yyyy = dt.getFullYear();
  const mm = String(dt.getMonth()+1).padStart(2,"0");
  const dd = String(dt.getDate()).padStart(2,"0");
  const hh = String(dt.getHours()).padStart(2,"0");
  const mi = String(dt.getMinutes()).padStart(2,"0");
  const data = `${yyyy}-${mm}-${dd}`;
  const horario = `${hh}:${mi}`;

  const out = await http("/agendamentos", {
    method: "POST",
    body: JSON.stringify({
      servicoId,
      barbeiroId,
      data,
      horario
    })
  });

  const mapped = toLegacyAppointment(out);
  __cache.appointments = [mapped, ...(__cache.appointments||[])];
  saveCache();
  return mapped;
}

export function addReview(){ /* não usado na V5 */ }

export async function refreshAdminReport(){
  try {
    const r = await http("/admin/relatorio", { method:"GET" });
    // backend: { totalAgendamentos, concluidos, faturamentoTotal }
    __cache.adminReport = {
      total: r.faturamentoTotal || 0,
      doneCount: r.concluidos || 0,
      byService: {}
    };
    saveCache();
  } catch (e) {
    // mantém o que tem
  }
}

export function adminReport(){
  return __cache.adminReport || { total:0, doneCount:0, byService:{} };
}