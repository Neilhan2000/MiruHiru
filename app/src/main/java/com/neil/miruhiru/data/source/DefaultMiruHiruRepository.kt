package com.neil.miruhiru.data.source

class DefaultMiruHiruRepository(
    private val miruHiruRemoteDataSource: MiruHiruDataSource
) : MiruHiruRepository {
    override fun test() {
        return miruHiruRemoteDataSource.test()
    }
}