const mensajes = document.getElementById('mensajes');
const formulario = document.getElementById('formulario');
const entrada = document.getElementById('mensaje');
const enviar = document.getElementById('enviar');

const SALUDO = 'Hola, soy MercaBot. Puedo mostrarte el catálogo y ayudarte a hacer un pedido a domicilio. '
    + 'Para empezar un pedido dime tu número de teléfono.';

// Identificador de la conversacion: el servidor guarda la memoria del hilo bajo esta clave
let conversacionId = nuevaConversacionId();

function nuevaConversacionId() {
  return crypto.randomUUID ? crypto.randomUUID() : `c-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

// Todo el texto se inserta con textContent: lo que escriba el usuario o el modelo nunca se interpreta como HTML
function agregarMensaje(autor, texto, herramientas = []) {
  const burbuja = document.createElement('article');
  burbuja.className = `mensaje mensaje--${autor}`;

  const cuerpo = document.createElement('p');
  cuerpo.textContent = texto;
  burbuja.appendChild(cuerpo);

  if (herramientas.length > 0) {
    const detalle = document.createElement('details');
    detalle.className = 'herramientas';
    detalle.open = true;
    const resumen = document.createElement('summary');
    resumen.textContent = `Herramientas MCP usadas (${herramientas.length})`;
    detalle.appendChild(resumen);
    const lista = document.createElement('ol');
    for (const h of herramientas) {
      const item = document.createElement('li');
      const nombre = document.createElement('code');
      nombre.textContent = h.herramienta;
      item.appendChild(nombre);
      item.appendChild(document.createTextNode(` ${h.argumentos}`));
      if (h.error) {
        const marca = document.createElement('span');
        marca.className = 'herramientas__error';
        marca.textContent = ' · devolvió error';
        item.appendChild(marca);
      }
      lista.appendChild(item);
    }
    detalle.appendChild(lista);
    burbuja.appendChild(detalle);
  }

  mensajes.appendChild(burbuja);
  mensajes.scrollTop = mensajes.scrollHeight;
  return burbuja;
}

function ocupado(estado) {
  enviar.disabled = estado;
  entrada.disabled = estado;
  if (!estado) entrada.focus();
}

formulario.addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const texto = entrada.value.trim();
  if (!texto) return;

  agregarMensaje('usuario', texto);
  entrada.value = '';
  ocupado(true);
  const pensando = agregarMensaje('pensando', 'MercaBot está pensando…');

  try {
    const respuesta = await fetch('/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ conversacionId, mensaje: texto }),
    });
    const datos = await respuesta.json();
    pensando.remove();
    if (respuesta.ok) {
      agregarMensaje('asistente', datos.respuesta, datos.herramientas || []);
    } else {
      agregarMensaje('error', datos.error || 'Ocurrió un problema. Inténtalo de nuevo.');
    }
  } catch (e) {
    pensando.remove();
    agregarMensaje('error', 'No se pudo contactar al chatbot. Comprueba que la aplicación siga encendida.');
  } finally {
    ocupado(false);
  }
});

// Enter envia; Mayus+Enter hace salto de linea
entrada.addEventListener('keydown', (evento) => {
  if (evento.key === 'Enter' && !evento.shiftKey) {
    evento.preventDefault();
    formulario.requestSubmit();
  }
});

document.getElementById('nueva').addEventListener('click', () => {
  conversacionId = nuevaConversacionId();
  mensajes.replaceChildren();
  agregarMensaje('asistente', SALUDO);
  entrada.focus();
});

agregarMensaje('asistente', SALUDO);
entrada.focus();
