package com.dua3.utility.rest;

/**
 * The part of an HTTP request in which an endpoint parameter is placed.
 */
public enum RestParameterLocation {
    /** A value substituted into a URI template. */
    PATH,
    /** A value appended to the request URI's query string. */
    QUERY,
    /** An HTTP request header. */
    HEADER,
    /** A value appended to the {@code Cookie} request header. */
    COOKIE
}
