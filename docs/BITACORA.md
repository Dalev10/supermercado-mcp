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

**Commit:** `4cd2276` (commit inicial de `main`; el repositorio git se creó al empezar la Fase 1)

## Fase 1. Repositorio y andamiaje (2026-11-15)

**Qué se hizo:**

1. Se inició el repositorio git local con rama `main` y se añadió el remoto de GitHub.
2. Se generaron los dos proyectos Maven con Spring Initializr: `mcp-server` (Web, Data JPA, Validation, MySQL Driver) y `chatbot-client` (Web, Validation), ambos con Java 21 y empaquetado jar.
3. Se ajustó cada `pom.xml` a Spring Boot 3.5.16 y se añadieron el BOM de Spring AI 1.1.8 y los starters: `spring-ai-starter-mcp-server-webmvc` en el servidor; `spring-ai-starter-mcp-client` y `spring-ai-starter-model-google-genai` en el chatbot.
4. Se crearon `.gitignore`, `.env.example`, `LICENSE`, `README.md` inicial y `CLAUDE.md`.
5. Se compilaron ambos módulos.

**Por qué:** tener desde el principio la estructura definitiva del monorepo y comprobar que el conjunto de versiones elegido (D1.4) se resuelve y compila antes de escribir lógica. Dos proyectos Maven independientes permiten arrancar servidor y chatbot por separado, que es como se relacionan un servidor MCP y su host.

**Comandos:**

```bash
git init -b main
git remote add origin git@github.com:Dalev10/supermercado-mcp.git

curl -o mcp-server.zip https://start.spring.io/starter.zip \
  -d type=maven-project -d language=java -d javaVersion=21 -d packaging=jar \
  -d groupId=com.udea -d artifactId=mcp-server -d name=supermercado-mcp \
  -d packageName=com.udea.supermercado -d baseDir=mcp-server \
  -d dependencies=web,data-jpa,validation,mysql
curl -o chatbot-client.zip https://start.spring.io/starter.zip \
  -d type=maven-project -d language=java -d javaVersion=21 -d packaging=jar \
  -d groupId=com.udea -d artifactId=chatbot-client -d name=chatbot \
  -d packageName=com.udea.supermercado.chatbot -d baseDir=chatbot-client \
  -d dependencies=web,validation
unzip mcp-server.zip && unzip chatbot-client.zip

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
cd mcp-server && ./mvnw -q -DskipTests package
cd ../chatbot-client && ./mvnw -q -DskipTests package
```

**Resultado:** ambos módulos compilan y generan su jar ejecutable. Maven resolvió Spring Boot 3.5.16, Spring AI 1.1.8, el SDK de MCP para Java 0.18.3 y el SDK `google-genai` 1.37.0.

**Problemas y solución:**

- start.spring.io rechazó `bootVersion=3.5.16` con el mensaje "Spring Boot compatibility range is >=4.0.0": hoy solo genera proyectos con Spring Boot 4.x. Solución: se generó la estructura con la versión por defecto (para obtener el wrapper de Maven y las clases base) y se reescribió el `pom.xml` tomando como base el del PDF: padre 3.5.16, `spring-boot-starter-web` en lugar de `spring-boot-starter-webmvc`, y un único `spring-boot-starter-test` en lugar de los starters de prueba separados de Boot 4.
- Las pruebas se omiten en esta fase (`-DskipTests`) porque la prueba `contextLoads` generada necesita la base de datos y la clave de Gemini, que llegan en fases posteriores.

**Evidencias:** `docs/evidencias/f1-compilacion.txt`

**Commit:** `70150e2` en la rama `fase-1-andamiaje`
