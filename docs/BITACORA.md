# Bitácora de desarrollo

Registro paso a paso de lo que se hizo en cada fase, por qué, con qué comandos y con qué resultado. Las decisiones se detallan en `DECISIONES.md` y las pruebas de que algo funciona están en `evidencias/`.

## Fase 0. Verificación del entorno (2026-11-15)

**Qué se hizo:** se comprobó que la máquina tiene lo necesario para el proyecto: sistema operativo, JDK 21, Docker con Compose, Node.js, git, GitHub CLI, puertos libres y Claude Desktop. También se leyeron completos la guía de desarrollo y el PDF del laboratorio.

**Por qué:** un fallo de entorno descubierto a mitad del desarrollo cuesta más que detectarlo antes de escribir código, y varias decisiones posteriores dependen de lo instalado (puerto de MySQL, forma de publicar en GitHub, ruta de `npx` para Claude Desktop).

**Comandos:**

```bash
lsb_release -a
java -version && echo "$JAVA_HOME"
/usr/lib/jvm/java-21-openjdk-amd64/bin/java -version
/usr/lib/jvm/java-21-openjdk-amd64/bin/javac -version
docker --version && docker compose version && docker ps
docker run --rm hello-world
node --version && npm --version
git --version && git config user.name && git config user.email
gh --version
ss -ltnp | grep -E ':(3306|8080|8888)\b' || echo "puertos libres"
```

**Resultado:**

| Requisito | Estado |
|---|---|
| Ubuntu 22.04.5 LTS | Cumple |
| JDK 21 | Instalado (OpenJDK 21.0.13); no es el predeterminado, se usa fijando `JAVA_HOME` |
| Docker 29.9.2 y Compose v5.6.1 | Cumple; `hello-world` corre sin `sudo` |
| Node v22.22.3 y npm 10.9.8 (nvm) | Cumple |
| git 2.34.1 con nombre y correo | Cumple |
| GitHub CLI | No instalado; se decidió trabajar sin él (D0.3) |
| Puertos 3306, 8080 y 8888 | Libres |
| Claude Desktop | No instalado; se aplaza a la Fase 6 (D0.4) |
| API key de Gemini | El estudiante confirma que la tiene |

**Problemas y solución:**

- El `java` predeterminado del sistema es el 17 y `JAVA_HOME` estaba vacío. Solución: exportar `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` en la terminal del proyecto, sin tocar el predeterminado del sistema.
- GitHub CLI no está instalado. Solución: el repositorio se creará a mano en github.com y se hará push con git (D0.3).
- El tiempo disponible es de pocas horas porque la entrega es el mismo día. Solución: seguir la ruta crítica de la guía (D0.5).

**Evidencias:** `docs/evidencias/f0-entorno.txt`

**Commit:** se incluye en el primer commit de la Fase 1, porque el repositorio git se crea en esa fase.
