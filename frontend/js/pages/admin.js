import { el, money, mountReveal } from "../ui/ui.js";
import * as api from "../data/api.js";

export function AdminPage(ctx){
  const wrap = el("div", {});
  wrap.append(
    el("h1",{class:"h1 reveal"},"Painel do Administrador"),
    el("p",{class:"sub reveal"},"Relatórios e gestão (API).")
  );

  const doneEl = el("strong",{}, "0");
  const totalEl = el("strong",{}, money(0));

  const card = el("div",{class:"card reveal"});
  card.append(
    el("div",{class:"kv"}, [
      el("i",{"data-lucide":"calendar"}),
      el("div",{}, [el("small",{},"Atendimentos concluídos"), el("br"), doneEl]),
      el("div",{class:"right"},"")
    ]),
    el("div",{class:"hr"}),
    el("div",{class:"kv"}, [
      el("i",{"data-lucide":"star"}),
      el("div",{}, [el("small",{},"Faturamento (concluídos)"), el("br"), totalEl]),
      el("div",{class:"right"},"")
    ])
  );

  wrap.append(el("div",{class:"section"}, [card]));

  // Atualiza via API
  (async ()=>{
    try{
      await api.refreshAdminReport();
      const report = api.adminReport();
      doneEl.textContent = String(report.doneCount || 0);
      totalEl.textContent = money(report.total || 0);
      try{ window.lucide?.createIcons?.(); }catch(e){}
    }catch(e){}
  })();

  mountReveal(wrap);
  return wrap;
}
