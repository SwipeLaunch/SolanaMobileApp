package com.anonymous.SolanaMobileApp

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    
    private const val SUPABASE_URL = "https://hhihnakkhihvfxyhuetq.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhoaWhuYWtraGlodmZ4eWh1ZXRxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTM4NzIxNDAsImV4cCI6MjA2OTQ0ODE0MH0.bUHPq-SXJtQnPKzbrvjYISij6_F40cfeS6eoaZIuyYI"
    
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        // Note: Using default configuration for now due to emulator network issues
        // In production, would add custom timeout and retry logic
    }
}