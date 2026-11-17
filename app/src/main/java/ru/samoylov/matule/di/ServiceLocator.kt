package ru.samoylov.matule.di

import android.content.Context
import ru.samoylov.matule.data.local.HomeCache
import ru.samoylov.matule.data.local.PreferencesStorage
import ru.samoylov.matule.data.repository.AuthRepositoryImpl
import ru.samoylov.matule.data.repository.CartRepositoryImpl
import ru.samoylov.matule.data.repository.CatalogRepositoryImpl
import ru.samoylov.matule.data.repository.ProfileRepositoryImpl
import ru.samoylov.matule.data.repository.ProjectRepositoryImpl
import ru.samoylov.matule.domain.repository.AuthRepository
import ru.samoylov.matule.domain.repository.CartRepository
import ru.samoylov.matule.domain.repository.CatalogRepository
import ru.samoylov.matule.domain.repository.ProfileRepository
import ru.samoylov.matule.domain.repository.ProjectRepository
import ru.samoylov.matule.domain.usecase.CompleteRegistrationUseCase
import ru.samoylov.matule.domain.usecase.CreateOrderUseCase
import ru.samoylov.matule.domain.usecase.CreateProjectUseCase
import ru.samoylov.matule.domain.usecase.GetCatalogUseCase
import ru.samoylov.matule.domain.usecase.GetHomeDataUseCase
import ru.samoylov.matule.domain.usecase.GetProductDetailsUseCase
import ru.samoylov.matule.domain.usecase.GetProfileUseCase
import ru.samoylov.matule.domain.usecase.GetProjectsUseCase
import ru.samoylov.matule.domain.usecase.GetStartDestinationUseCase
import ru.samoylov.matule.domain.usecase.LoginUseCase
import ru.samoylov.matule.domain.usecase.LogoutUseCase
import ru.samoylov.matule.domain.usecase.ManageCartUseCase
import ru.samoylov.matule.domain.usecase.PinCodeUseCase
import ru.samoylov.matule.domain.usecase.ProfileDraftUseCase
import ru.samoylov.matule.domain.usecase.RegisterUseCase
import ru.samoylov.matule.domain.usecase.SaveProfileUseCase
import ru.samoylov.matule.domain.usecase.SearchProductsUseCase
import ru.samoylov.matule.network.ApiConfig
import ru.samoylov.matule.network.MatuleApiService
import ru.samoylov.matule.network.NetworkProvider

/**
 * Сборка зависимостей приложения.
 *
 * Здесь и только здесь конкретные реализации подставляются вместо
 * интерфейсов: экраны получают уже готовые сценарии и ничего не знают
 * ни про Retrofit, ни про SharedPreferences. Чтобы заменить сетевую
 * библиотеку или хранилище, правится только этот файл.
 */
object ServiceLocator {

    /**
     * true — работать с тестовыми данными FakeApiService (сервера нет).
     * false — настоящий сервер через Retrofit (адрес в ApiConfig.DEFAULT_BASE_URL).
     * Это единственное место, которое нужно поменять при появлении сервера.
     */
    private const val USE_FAKE_API = true

    private lateinit var appContext: Context

    /**
     * Инициализация при старте приложения.
     * @param context контекст приложения
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // ===================== ИСТОЧНИКИ ДАННЫХ =====================

    val storage: PreferencesStorage by lazy { PreferencesStorage(appContext) }

    private val homeCache: HomeCache by lazy { HomeCache(storage) }

    /** Сетевой слой отдаётся как абстракция, а не как конкретная реализация. */
    private val api: MatuleApiService by lazy {
        if (USE_FAKE_API) {
            FakeApiService(tokenStore = storage)
        } else {
            NetworkProvider.create(
                tokenStore = storage,
                config = ApiConfig(),
                enableLogging = true
            )
        }
    }

    // ===================== РЕПОЗИТОРИИ =====================

    private val authRepository: AuthRepository by lazy { AuthRepositoryImpl(api, storage) }
    private val profileRepository: ProfileRepository by lazy { ProfileRepositoryImpl(api, storage) }
    private val catalogRepository: CatalogRepository by lazy { CatalogRepositoryImpl(api, homeCache) }
    private val cartRepository: CartRepository by lazy { CartRepositoryImpl(api) }
    private val projectRepository: ProjectRepository by lazy { ProjectRepositoryImpl(api) }

    // ===================== СЦЕНАРИИ =====================

    val loginUseCase: LoginUseCase get() = LoginUseCase(authRepository)
    val registerUseCase: RegisterUseCase get() = RegisterUseCase(authRepository)
    val logoutUseCase: LogoutUseCase get() = LogoutUseCase(authRepository)
    val startDestinationUseCase: GetStartDestinationUseCase
        get() = GetStartDestinationUseCase(authRepository)
    val pinCodeUseCase: PinCodeUseCase get() = PinCodeUseCase(authRepository)
    val completeRegistrationUseCase: CompleteRegistrationUseCase
        get() = CompleteRegistrationUseCase(authRepository, profileRepository)

    val getProfileUseCase: GetProfileUseCase get() = GetProfileUseCase(profileRepository)
    val saveProfileUseCase: SaveProfileUseCase get() = SaveProfileUseCase(profileRepository)
    val profileDraftUseCase: ProfileDraftUseCase get() = ProfileDraftUseCase(profileRepository)

    val getHomeDataUseCase: GetHomeDataUseCase get() = GetHomeDataUseCase(catalogRepository)
    val getCatalogUseCase: GetCatalogUseCase get() = GetCatalogUseCase(catalogRepository)
    val searchProductsUseCase: SearchProductsUseCase
        get() = SearchProductsUseCase(catalogRepository)
    val getProductDetailsUseCase: GetProductDetailsUseCase
        get() = GetProductDetailsUseCase(catalogRepository)

    val manageCartUseCase: ManageCartUseCase get() = ManageCartUseCase(cartRepository)
    val createOrderUseCase: CreateOrderUseCase get() = CreateOrderUseCase(cartRepository)

    val getProjectsUseCase: GetProjectsUseCase get() = GetProjectsUseCase(projectRepository)
    val createProjectUseCase: CreateProjectUseCase get() = CreateProjectUseCase(projectRepository)
}
