package com.eomaxl.springkotlincorountines.blocking.controller

import com.eomaxl.springkotlincorountines.blocking.model.UserJpa
import com.eomaxl.springkotlincorountines.blocking.repository.BlockingAvatarService
import com.eomaxl.springkotlincorountines.blocking.repository.BlockingEnrollmentService
import com.eomaxl.springkotlincorountines.blocking.repository.BlockingUserDao
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.supplyAsync
import javax.transaction.Transactional

@RestController
class BlockingUserController(
    private val blockingUserDao: BlockingUserDao,
    private val blockingAvatarService: BlockingAvatarService,
    private val blockingEnrollmentService: BlockingEnrollmentService
) {

    @GetMapping("/blocking/users/{user-id}")
    @ResponseBody
    fun getUser(@PathVariable("user-id") id: Long = 0): UserJpa?=
        blockingUserDao.findById(id).toNullable()

    @GetMapping("/blocking/users")
    @ResponseBody
    fun getUsers(): List<UserJpa> = blockingUserDao.findAll()

    @GetMapping("/blocking/users/{user-id}/sync-avatar")
    @ResponseBody
    fun syncAvatar(@PathVariable("user-id") id: Long = 0,@RequestParam(required=false) delay:Long? = null):UserJpa? =
        blockingUserDao.findById(id).toNullable()?.let{
            val avatar = blockingAvatarService.randomAvatar(delay)
            blockingUserDao.save(it.copy(avatarUrl = avatar.url))
        }

    @PostMapping("/blocking/users")
    @ResponseBody
    @Transactional
    fun storeUser(@ResponseBody user: UserJpa, @RequestParam(required = false) delay:Long? = null):UserJpa{
        val emailVerified = blockingEnrollmentService.verifyEmail(user.email, delay)
        val avatarUrl = user.avatarUrl ?: blockingAvatarService.randomAvatar(delay).url
        return blockingUserDao.save(user.copy(avatarUrl = avatarUrl, emailVerified = emailVerified))
    }

    @PostMapping("/futures/users")
    @ResponseBody
    fun storeUserFutures(@RequestBody user: UserJpa, @RequestParam(required = false) delay:Long? = null): UserJpa {
        val emailVerifiedF = CompletableFuture.supplyAsync { blockingEnrollmentService.verifyEmail(user.email, delay) }
        val avatuarUrl = if (user.avatarUrl != null) CompletableFuture.completedFuture(user.avatarUrl) else
            CompletableFuture.supplyAsync { blockingAvatarService.randomAvatar(delay).url }
        val combinedF = avatuarUrl.thenCombineAsync(emailVerifiedF) { avatuarUrl, emailVerified -> avatuarUrl to emailVerified }
        val (avatarUrl, emailVerified) = combinedF.join()
        return blockingUserDao.save(user.copy(avatarUrl = avatarUrl, emailVerified = emailVerified))
    }

    @PostMapping("/futures-mdc/users")
    @ResponseBody
    fun storeUserFuturesMdc(@RequestBody user: UserJpa, @RequestParam(required = false) delay:Long? = null): UserJpa {
        val avatarUrlF = supplyAsync { blockingAvatarService.randomAvatar(delay).url }
        val combinedF = supplyAsync { blockingEnrollmentService.verifyEmail(user.email, delay) }
            .thenCombineAsync(avatarUrlF) { emailVerified, avatarUrl -> avatarUrl to emailVerified }
        val (avatarUrl, emailVerified) = combinedF.join()
        return blockingUserDao.save(user.copy(avatarUrl = avatarUrl, emailVerified = emailVerified))
    }

}