package com.anonymous.SolanaMobileApp

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        // Note: Using default configuration for now due to emulator network issues
        // In production, would add custom timeout and retry logic
    }
}