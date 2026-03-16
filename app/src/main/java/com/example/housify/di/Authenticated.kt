package com.example.housify.di

/**
 * A Retrofit annotation to mark that a request requires authentication.
 * The AuthInterceptor will look for this.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Authenticated