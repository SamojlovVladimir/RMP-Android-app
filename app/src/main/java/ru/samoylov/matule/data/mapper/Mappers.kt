package ru.samoylov.matule.data.mapper

import ru.samoylov.matule.domain.model.CartItem
import ru.samoylov.matule.domain.model.Product
import ru.samoylov.matule.domain.model.Project
import ru.samoylov.matule.domain.model.Promo
import ru.samoylov.matule.domain.model.User
import ru.samoylov.matule.network.dto.CartItemDto
import ru.samoylov.matule.network.dto.CreateProjectRequest
import ru.samoylov.matule.network.dto.ProductDto
import ru.samoylov.matule.network.dto.ProjectDto
import ru.samoylov.matule.network.dto.PromoDto
import ru.samoylov.matule.network.dto.UpdateProfileRequest
import ru.samoylov.matule.network.dto.UserDto

/**
 * Перевод моделей сервера в доменные модели и обратно.
 *
 * Единственное место в приложении, где встречаются оба типа моделей.
 * Благодаря этому доменный слой не зависит от формата ответов сервера:
 * при изменении API правится только этот файл.
 */

/** Переводит пользователя сервера в доменную модель. */
fun UserDto.toDomain(): User = User(
    id = id,
    email = email.orEmpty(),
    name = name.orEmpty(),
    surname = surname.orEmpty(),
    patronymic = patronymic.orEmpty(),
    birthDate = birthDate.orEmpty(),
    gender = gender.orEmpty()
)

/** Собирает запрос изменения профиля из доменной модели. */
fun User.toUpdateRequest(): UpdateProfileRequest = UpdateProfileRequest(
    name = name,
    surname = surname,
    patronymic = patronymic,
    birthDate = birthDate,
    gender = gender,
    email = email
)

/** Переводит акцию сервера в доменную модель. */
fun PromoDto.toDomain(): Promo = Promo(
    id = id,
    title = title.orEmpty(),
    subtitle = subtitle.orEmpty(),
    price = price,
    imageUrl = image.orEmpty()
)

/** Переводит товар сервера в доменную модель. */
fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title.orEmpty(),
    category = category.orEmpty(),
    description = description.orEmpty(),
    price = price,
    consumption = consumption.orEmpty(),
    imageUrl = image.orEmpty()
)

/** Переводит позицию корзины сервера в доменную модель. */
fun CartItemDto.toDomain(): CartItem = CartItem(
    id = id,
    productId = product,
    title = title.orEmpty(),
    price = price,
    quantity = quantity
)

/** Переводит проект сервера в доменную модель. */
fun ProjectDto.toDomain(): Project = Project(
    id = id,
    title = title.orEmpty(),
    type = type.orEmpty(),
    startDate = startDate.orEmpty(),
    endDate = endDate.orEmpty(),
    target = target.orEmpty(),
    source = source.orEmpty(),
    category = category.orEmpty(),
    photoUrl = photo.orEmpty(),
    updatedAt = updated.orEmpty()
)

/** Собирает запрос создания проекта из доменной модели. */
fun Project.toCreateRequest(): CreateProjectRequest = CreateProjectRequest(
    title = title,
    type = type,
    startDate = startDate,
    endDate = endDate,
    target = target,
    source = source,
    category = category
)
