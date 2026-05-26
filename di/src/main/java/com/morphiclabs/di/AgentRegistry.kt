package com.morphiclabs.di

import com.morphiclabs.core.base.AgentContract

/**
 * A registry for all agents implementing the AgentContract.
 * This class allows the Shell to discover and interact with various agents.
 */
class AgentRegistry {
    private val agents = mutableListOf<AgentContract>()

    /**
     * Registers an agent with the registry.
     * @param agent The agent to register.
     */
    fun registerAgent(agent: AgentContract) {
        agents.add(agent)
    }

    /**
     * Returns a list of all registered agents.
     * @return A list of AgentContract instances.
     */
    fun getAgents(): List<AgentContract> {
        return agents.toList()
    }

    /**
     * Finds the first agent that can handle the given command.
     * @param command The command string to check.
     * @return An AgentContract instance if found, or null otherwise.
     */
    suspend fun findAgentToHandle(command: String): AgentContract? {
        return agents.firstOrNull { it.canHandle(command) }
    }
}
