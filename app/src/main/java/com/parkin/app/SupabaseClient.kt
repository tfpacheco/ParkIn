package com.parkin.app

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = "",
    supabaseKey = ""
) {
    install(Postgrest)
    install(Auth) {
        alwaysAutoRefresh = true
    }
}
