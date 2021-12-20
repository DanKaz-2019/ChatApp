package com.baiganov.fintech.data.repository

import com.baiganov.fintech.data.datasource.PeopleLocalDataSource
import com.baiganov.fintech.data.datasource.PeopleRemoteDataSource
import com.baiganov.fintech.data.db.entity.UserEntity
import com.baiganov.fintech.data.model.response.User
import com.baiganov.fintech.domain.repository.PeopleRepository
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

class PeopleRepositoryImpl @Inject constructor(
    private val peopleRemoteDataSource: PeopleRemoteDataSource,
    private val localDataSource: PeopleLocalDataSource
) : PeopleRepository {

    override fun getUsers() = localDataSource.getUsers()

    override fun searchUser(name: String): Flowable<List<UserEntity>> = localDataSource.searchUsers(
        "$name%"
    )

    override fun loadUsers(): Completable {
        return peopleRemoteDataSource.getUsers()
            .flattenAsObservable { usersResponse ->
                usersResponse.users.filter { it.isActive }.filter { !it.isBot }
            }
            .flatMapSingle { user ->
                getStatus(user)
            }
            .toList()
            .flatMapCompletable { users ->
                localDataSource.saveUsers(users)
            }
    }

    private fun getStatus(user: User): Single<UserEntity> {
        return peopleRemoteDataSource.getUserPresence(user.userId.toString())
            .zipWith(Single.just(user)) { userPresenceResponse, _ ->
                UserEntity(
                    userId = user.userId,
                    avatarUrl = user.avatarUrl,
                    email = user.email,
                    fullName = user.fullName,
                    isActive = user.isActive,
                    isBot = user.isBot,
                    status = userPresenceResponse.presence.aggregated.status
                )
            }
    }
}