package com.morphiclabs.core.base

interface AgentContract {
    /**
     * Checks if the agent can handle a given command or input.
     * @param command The command or input string to check.
     * @return True if the agent can handle it, false otherwise.
     */
    suspend fun canHandle(command: String): Boolean

    /**
     * Executes the agent's logic based on the provided input.
     * @param input The input string for the agent to process.
     * @return A string representing the agent's response.
     */
    suspend fun execute(input: String): String
}
