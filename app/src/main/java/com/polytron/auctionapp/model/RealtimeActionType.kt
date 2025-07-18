package com.polytron.auctionapp.model

public enum class RealtimeActionType {
    CONNECT,

    CREATE,

    UPDATE,

    DELETE;

    /**
     * Returns weather or not the event type is capable of containing a body or
     * record
     */
    public fun isBodyEvent(): Boolean {
        return when (this) {
            CONNECT -> false
            else -> {
                true
            }
        }
    }
}