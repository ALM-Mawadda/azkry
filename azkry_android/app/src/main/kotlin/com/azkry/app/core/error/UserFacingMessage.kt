package com.azkry.app.core.error

/**
 * Marker for exceptions whose `message` is already localized and safe to show
 * directly to users. Anything else must be surfaced through a fallback string
 * resource — raw exception messages never reach the UI.
 */
interface UserFacingMessage
