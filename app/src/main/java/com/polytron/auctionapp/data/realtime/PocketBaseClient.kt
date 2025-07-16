package com.yourapp.pocketbase

class PocketBaseClient(val baseUrl: String) {
    fun collection(name: String): PocketBaseCollection {
        return PocketBaseCollection(baseUrl, name)
    }
}
