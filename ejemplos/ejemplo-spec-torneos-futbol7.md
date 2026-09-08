# Spec: Gestión de torneos de fútbol 7 (inscripción y planteles)

## Problem Statement

Hoy la inscripción y armado de equipos para un torneo de fútbol 7 se maneja de forma
manual (planillas, mensajes), sin una única fuente de verdad ni validación previa. Esto
genera equipos incompletos, planteles mal armados (sin arquero, con más suplentes de los
permitidos), nombres de equipo duplicados, jugadores fichados en más de un equipo a la
vez, y torneos que arrancan sin cumplir el cupo mínimo de equipos — todo detectado tarde,
cuando ya es difícil de corregir.

## Solution

Un sistema que modela Torneo, Equipo, Jugador, DirectorTecnico y EntidadComercial con
reglas estructurales y de negocio explícitas (composición de plantel, unicidad de
nombres/DNI, ciclo de vida ARMADO/CERRADO del torneo), de forma que el organizador solo
pueda cerrar la inscripción de un torneo cuando todos los equipos cumplen los requisitos
reglamentarios, evitando que el sistema llegue a un estado inconsistente.

## User Stories

1. Como organizador de torneo, quiero crear un torneo con nombre y cupo mínimo/máximo de equipos, para poder empezar a recibir inscripciones.
2. Como organizador, quiero que el nombre del torneo sea único, para evitar confusión entre torneos.
3. Como organizador, quiero ver el estado actual del torneo (ARMADO/CERRADO), para saber si todavía puedo modificar equipos.
4. Como organizador, quiero cerrar la inscripción de un torneo, para fijar definitivamente el conjunto de equipos participantes.
5. Como organizador, quiero que el sistema me impida cerrar un torneo si hay equipos incompletos, para no arrancar el torneo con datos inválidos.
6. Como organizador, quiero que el sistema me impida cerrar un torneo si no se alcanzó el mínimo de equipos configurado, para asegurar un mínimo de competitividad.
7. Como organizador, quiero que una vez cerrado el torneo no se puedan agregar ni quitar equipos, para preservar la integridad de la competencia.
8. Como organizador, quiero crear un equipo dentro de un torneo abierto, con un nombre, para poder empezar a cargar su plantel.
9. Como organizador, quiero que el nombre del equipo sea único dentro de su torneo (pero no necesariamente a nivel global), para poder reutilizar nombres comunes entre torneos distintos.
10. Como organizador, quiero asociar opcionalmente un equipo a una entidad o firma comercial, para reflejar equipos auspiciados.
11. Como organizador, quiero que una misma entidad comercial pueda auspiciar más de un equipo, para representar sponsors con múltiples equipos.
12. Como organizador, quiero poder cargar el plantel de un equipo de forma incremental (sin exigir que esté completo desde el primer momento), para poder armarlo a lo largo del período de inscripción.
13. Como organizador, quiero que el sistema valide que un equipo tenga exactamente 7 jugadores titulares y hasta 3 suplentes antes de considerarlo completo, para cumplir el reglamento de fútbol 7.
14. Como organizador, quiero que el sistema valide que exactamente uno de los titulares sea arquero, para asegurar que el equipo pueda jugar.
15. Como organizador, quiero que cada equipo tenga asignado exactamente un director técnico antes de considerarse completo, para cumplir el requisito reglamentario.
16. Como organizador, quiero poder eliminar o reasignar equipos mientras el torneo esté abierto, para corregir errores de carga.
17. Como organizador, quiero registrar un jugador con nombre, DNI y fecha de nacimiento, para identificarlo unívocamente.
18. Como organizador, quiero que el DNI del jugador sea único en todo el sistema, para evitar duplicar la misma persona.
19. Como organizador, quiero asignar a cada jugador una posición fija (arquero, defensa, mediocampo, delantera), para reflejar su rol dentro del equipo.
20. Como organizador, quiero marcar a cada jugador como titular o suplente, para reflejar su lugar en el plantel.
21. Como organizador, quiero que el sistema impida fichar al mismo jugador (por DNI) en más de un equipo a la vez, para respetar la regla de fichaje único.
22. Como organizador, quiero poder reasignar un jugador de un equipo a otro mientras el torneo esté abierto, para corregir errores antes del cierre.
23. Como organizador, quiero que un jugador no pueda existir sin estar asociado a un equipo, para evitar datos huérfanos.
24. Como organizador, quiero registrar un director técnico con nombre y DNI, para identificarlo.
25. Como organizador, quiero que un director técnico esté asociado a un único equipo a la vez, para respetar la exclusividad del cargo.
26. Como organizador, quiero poder reasignar el DT de un equipo mientras el torneo esté abierto, para corregir errores de carga.
27. Como organizador, quiero registrar una entidad comercial con nombre/razón social y opcionalmente rubro y contacto, para llevar un registro de sponsors.
28. Como organizador, quiero ver qué equipos auspicia una entidad comercial, para tener visibilidad de sus patrocinios.
29. Como organizador, quiero recibir un mensaje claro indicando qué le falta a un equipo incompleto, para poder corregirlo antes del cierre del torneo.
30. Como organizador, quiero que las reglas de plantel (7 titulares, 1 arquero, hasta 3 suplentes, 1 DT) se validen de forma consistente sin importar la vía de carga (manual, importación, etc.), para no depender de que cada pantalla repita la lógica.

## Implementation Decisions

- **Entidades del dominio**: Torneo, Equipo, Jugador, DirectorTecnico, EntidadComercial. Modelo completo (atributos, cardinalidades, diagrama Mermaid) documentado en `modelo-er-torneos-futbol7.md`.
- **Alcance**: solo inscripción y gestión de plantel. Partidos, fixture, resultados y tabla de posiciones quedan fuera (posible extensión futura sobre este mismo modelo).
- **Fichaje** (Jugador/DirectorTecnico → Equipo): relación de estado actual mediante FK directa, sin historial de pases entre temporadas.
- **Cardinalidad Torneo–Equipo**: 1:N. El Equipo nace siempre asociado a un Torneo (FK obligatoria desde la creación); no existen equipos sin torneo.
- **Ciclo de vida del Torneo**: enum de estado `{ARMADO, CERRADO}`. En `ARMADO` se permite alta/baja/reasignación de equipos, jugadores y DT. La transición a `CERRADO` es una acción explícita del organizador, no automática por fecha.
- **Validación diferida**: la completitud del plantel (7 titulares + exactamente 1 arquero entre ellos + hasta 3 suplentes + 1 DT) no se exige en cada alta individual, sino como precondición de la transición `ARMADO → CERRADO`. Esto habilita estados intermedios "incompletos" durante la carga.
- **Unicidad**: `Torneo.nombre` único global; `Equipo.nombre` único dentro de su Torneo (no global); `Jugador.dni` único global; `DirectorTecnico.dni` único global.
- **DirectorTecnico–Equipo**: relación 1:1 exclusiva (un DT dirige a lo sumo un equipo vigente). Por simetría con Jugador, se asume que un DirectorTecnico se crea siempre asociado a un Equipo, sin DTs "libres" — asunción derivada durante el modelado, no confirmada con un usuario final real del sistema.
- **EntidadComercial–Equipo**: relación opcional (0..1 desde Equipo); una EntidadComercial puede auspiciar múltiples Equipos.
- **Posición del jugador**: enum cerrado `{ARQUERO, DEFENSA, MEDIOCAMPO, DELANTERA}`, sin catálogo extensible.
- **Rol de plantel** (`TITULAR`/`SUPLENTE`): atributo fijo del Jugador, no varía por partido — consistente con que la gestión de partidos está fuera de alcance.
- **Sin entidades "libres"**: ni Jugador ni DirectorTecnico pueden existir sin FK obligatoria a un Equipo desde su creación.

## Testing Decisions

- Un buen test verifica comportamiento externo observable (¿la operación se permite o se rechaza, y con qué resultado?), no detalles de implementación interna.
- **Seam elegido**: una capa de validación de dominio pura, desacoplada de persistencia y de transporte (API/UI). Ejemplos de funciones a testear: `validarPlantelCompleto(equipo)`, `tieneExactamenteUnArquero(titulares)`, `puedeCerrarInscripcion(torneo, equipos)`, `esFichajeValido(jugador, equipo)`.
- **Casos a cubrir por módulo**:
  - *Torneo*: cierre bloqueado por equipo incompleto; cierre bloqueado por no alcanzar `minEquipos`; cierre exitoso cuando todo es válido; nombre de torneo duplicado rechazado.
  - *Equipo*: nombre duplicado dentro del mismo torneo rechazado, pero permitido en torneos distintos; detección correcta de equipo incompleto (conteo de titulares/suplentes, ausencia de arquero, ausencia de DT).
  - *Jugador*: DNI duplicado (incluso entre equipos distintos) rechazado; reasignación de equipo bloqueada si el torneo está `CERRADO`; exactamente un arquero entre titulares.
  - *DirectorTecnico*: DNI duplicado rechazado; un DT no puede quedar asociado a más de un equipo simultáneamente.
- **Prior art**: no hay código en el repositorio todavía, por lo que no existe prior art interno. Estos serían los primeros tests del proyecto y deberían fijar el patrón (validación pura, sin mocks de persistencia) para módulos futuros, como la eventual gestión de partidos.

## Out of Scope

- Gestión de partidos, fixture, resultados y tabla de posiciones.
- Historial de fichajes entre temporadas/torneos (transferencias, bajas históricas).
- Que una misma persona sea Jugador y DirectorTecnico simultáneamente.
- Autenticación/autorización de usuarios y roles administrativos — no se definieron actores más allá de un "organizador" genérico.
- Definición de stack tecnológico, persistencia concreta o contrato de API — esta spec es agnóstica de implementación.

## Further Notes

- Esta especificación complementa el modelo entidad-relación en `modelo-er-torneos-futbol7.md`, que detalla atributos, cardinalidades y el diagrama Mermaid completo.
- No hay tracker de issues configurado en este repositorio (ni siquiera está inicializado como repositorio git), por lo que esta spec se publica como archivo markdown en el proyecto en lugar de un ticket con etiqueta `ready-for-agent`.
- Queda pendiente definir actores/roles de usuario (¿todo lo hace un único "organizador", o existe un rol de "delegado de equipo" con permisos acotados a su propio equipo?). Se asumió un actor único para esta versión de la spec.
