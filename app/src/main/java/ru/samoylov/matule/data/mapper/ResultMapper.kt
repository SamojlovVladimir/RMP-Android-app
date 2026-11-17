package ru.samoylov.matule.data.mapper

import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.network.NetworkError
import ru.samoylov.matule.network.NetworkResult

/**
 * Перевод результата сетевого слоя в результат доменного слоя.
 *
 * Вынесено в одну функцию, чтобы каждый репозиторий не повторял
 * одинаковую обработку ошибок (DRY). Заодно это граница, за которую
 * тип NetworkResult не проходит: доменный и presentation слои
 * о существовании сетевой библиотеки не знают.
 *
 * @param transform как превратить данные сети в доменную модель
 */
fun <T, R> NetworkResult<T>.toDomainResult(transform: (T) -> R): DomainResult<R> = when (this) {
    is NetworkResult.Success -> DomainResult.Success(transform(data))
    is NetworkResult.Failure -> DomainResult.Error(
        message = message,
        isAuthError = error == NetworkError.UNAUTHORIZED
    )
}
