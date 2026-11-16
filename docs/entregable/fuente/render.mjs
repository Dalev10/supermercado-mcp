// Abre una URL en el Chrome headless (puerto CDP) y guarda en PNG el recorte de un elemento.
// Uso: node render.mjs <puerto> <url> <selector> <salida.png> [ancho] [escala] [espera_ms]
import { writeFileSync } from 'node:fs';

const [puerto, url, selector, salida, ancho = '1400', escala = '2', espera = '1500'] = process.argv.slice(2);
const dormir = (ms) => new Promise((r) => setTimeout(r, ms));

const nueva = await (await fetch(`http://127.0.0.1:${puerto}/json/new?about:blank`, { method: 'PUT' })).json();
const ws = new WebSocket(nueva.webSocketDebuggerUrl);
await new Promise((ok, mal) => { ws.onopen = ok; ws.onerror = mal; });
let sec = 0;
const pend = new Map();
ws.onmessage = (ev) => { const m = JSON.parse(ev.data); if (m.id && pend.has(m.id)) { pend.get(m.id)(m); pend.delete(m.id); } };
const cdp = (method, params = {}) => new Promise((ok, mal) => {
  const id = ++sec;
  pend.set(id, (m) => (m.error ? mal(new Error(`${method}: ${m.error.message}`)) : ok(m.result)));
  ws.send(JSON.stringify({ id, method, params }));
});
const evaluar = async (expression) => (await cdp('Runtime.evaluate', { expression, returnByValue: true, awaitPromise: true })).result.value;

await cdp('Page.enable');
await cdp('Emulation.setDeviceMetricsOverride', { width: Number(ancho), height: Number(process.env.ALTO || 1000), deviceScaleFactor: Number(escala), mobile: false });
await cdp('Page.navigate', { url });
for (let i = 0; i < 120; i++) {
  if (await evaluar(`(() => { const e = document.querySelector(${JSON.stringify(selector)}); return !!e && e.getBoundingClientRect().height > 20 && document.readyState === 'complete'; })()`)) break;
  await dormir(250);
}
await dormir(Number(espera));
const caja = await evaluar(`(() => { const r = document.querySelector(${JSON.stringify(selector)}).getBoundingClientRect(); return { x: r.x + scrollX, y: r.y + scrollY, width: r.width, height: r.height }; })()`);
if (!caja) throw new Error('No se encontró ' + selector);
const pad = 12;
const { data } = await cdp('Page.captureScreenshot', {
  format: 'png', captureBeyondViewport: true,
  clip: { x: Math.max(0, caja.x - pad), y: Math.max(0, caja.y - pad), width: caja.width + 2 * pad, height: caja.height + 2 * pad, scale: 1 },
});
writeFileSync(salida, Buffer.from(data, 'base64'));
console.log('figura ->', salida, Math.round(caja.width), 'x', Math.round(caja.height));
await fetch(`http://127.0.0.1:${puerto}/json/close/${nueva.id}`);
ws.close();
