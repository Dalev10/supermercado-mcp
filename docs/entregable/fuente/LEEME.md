# Fuente del informe

`gen.js` genera `../Informe_Laboratorio_MCP.docx` con la librería `docx` de Node (versión 9.5.1):

```bash
npm install docx@9.5.1            # en una carpeta de trabajo, no en el sistema
node gen.js <carpeta-del-proyecto> ../figuras ../Informe_Laboratorio_MCP.docx
```

Las figuras de `../figuras/` se generaron con Chrome sin interfaz:

- `diagramas.py` escribe una página HTML por diagrama Mermaid.
- `render.mjs` abre cada página por el protocolo DevTools y guarda el elemento como PNG.
- `grafo-ramas.png` sale de `git log --graph main`.
- `pull-requests.png` es la lista pública de Pull Requests cerrados en GitHub.
