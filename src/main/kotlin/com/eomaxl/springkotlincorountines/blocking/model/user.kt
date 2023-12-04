package com.eomaxl.springkotlincorountines.blocking.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType


@Entity(name = "users")
data class UserJpa(
    @field: javax.persistence.Id @field: GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @field:Column(name = "user_name")
    val userName: String,
    val email: String,
    @field:Column(name="email_verified")
    val emailVerified:Boolean,
    @field:Column(name="avatar_url")
    val avatarUrl: String?) {
}
