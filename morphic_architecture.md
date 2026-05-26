# Arquitectura Morphic Labs (Feature-Modular & Shell-based)

## 1. Vision General
App basada en una arquitectura **Shell-Plugin**. El Shell es un lienzo en blanco (Unified Chat UI). Los módulos son Agentes independientes que inyectan lógica y capacidades (UI/Datos) al Shell de forma dinámica.

## 2. Estructura de Modulos
- **`:app` (The Shell):** Punto de entrada. Interfaz unificada. Solo renderiza lo que los Plugins le piden.
- **`:core-base`:** Contratos (Interfaces) compartidos. Define el `AgentContract` (lo que todo agente debe cumplir).
- **`:core-network`:** Punto único de acceso a LLMs (Gemini/Local LLMs).
- **`:core-database`:** Persistencia centralizada (Room) para datos compartidos (inventarios, configuraciones).
- **`:feature-{agent_name}`:** - Ejemplo: `:feature-inventory`, `:feature-sales`.
    - Contiene: Logica de negocio, Modelos de datos del agente, ViewModels.
    - Opcionalmente: "UI-Widgets" (componentes inyectables al Chat).
- **`:di`:** Inyeccion de dependencias centralizada para conectar el grafo.

## 3. Patron de Registro y Comunicacion
- **AgentRegistry:** El Shell consulta este registro para saber qué agentes están vivos.
- **AgentContract:** Todo agente debe implementar esta interfaz para que el Shell pueda "hablarle" (Ej: `canHandle(command)`, `execute(input)`).
- **MessageBus (Reactive):** El Shell y los Agentes se comunican vía eventos. Si un agente responde, el Shell recibe el evento y actualiza la UI.

## 4. Reglas de Oro (Para Aider y Desarrollo)
- **Independencia:** Un módulo `:feature` NUNCA debe importar otro `:feature`. Solo se comunican vía `:core-base`.
- **UI Inyectable:** El Shell no conoce la lógica del Agente. El Agente devuelve un "tipo de mensaje" (Texto, Tabla, Botón), y el Shell sabe cómo renderizar ese tipo.
- **Single Source of Truth:** Los datos que deben persistir viven en `:core-database`.
- **Desacoplamiento:** Si borras un módulo `:feature`, la app sigue compilando y funcionando perfectamente.
