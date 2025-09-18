package ru.sendsay.sdk

import android.content.Context
import android.util.Log
import ru.sendsay.sdk.repository.FontCache
import ru.sendsay.sdk.manager.AppInboxManager
import ru.sendsay.sdk.manager.AppInboxManagerImpl
import ru.sendsay.sdk.manager.BackgroundTimerManager
import ru.sendsay.sdk.manager.BackgroundTimerManagerImpl
import ru.sendsay.sdk.manager.CampaignManager
import ru.sendsay.sdk.manager.CampaignManagerImpl
import ru.sendsay.sdk.manager.ConnectionManager
import ru.sendsay.sdk.manager.ConnectionManagerImpl
import ru.sendsay.sdk.manager.EventManager
import ru.sendsay.sdk.manager.EventManagerImpl
import ru.sendsay.sdk.manager.EventManagerInAppMessageTrackingDelegate
import ru.sendsay.sdk.manager.FcmManager
import ru.sendsay.sdk.manager.FcmManagerImpl
import ru.sendsay.sdk.manager.FetchManager
import ru.sendsay.sdk.manager.FetchManagerImpl
import ru.sendsay.sdk.manager.FlushManager
import ru.sendsay.sdk.manager.FlushManagerImpl
import ru.sendsay.sdk.manager.InAppContentBlockManager
import ru.sendsay.sdk.manager.InAppContentBlockManagerImpl
import ru.sendsay.sdk.manager.InAppMessageManager
import ru.sendsay.sdk.manager.InAppMessageManagerImpl
import ru.sendsay.sdk.manager.InAppMessageTrackingDelegate
import ru.sendsay.sdk.manager.InitConfigManager
import ru.sendsay.sdk.manager.InitConfigManagerImpl
import ru.sendsay.sdk.manager.PushNotificationSelfCheckManager
import ru.sendsay.sdk.manager.PushNotificationSelfCheckManagerImpl
import ru.sendsay.sdk.manager.SegmentsManager
import ru.sendsay.sdk.manager.SegmentsManagerImpl
import ru.sendsay.sdk.manager.ServiceManager
import ru.sendsay.sdk.manager.ServiceManagerImpl
import ru.sendsay.sdk.manager.SessionManager
import ru.sendsay.sdk.manager.SessionManagerImpl
import ru.sendsay.sdk.manager.TrackingConsentManager
import ru.sendsay.sdk.manager.TrackingConsentManagerImpl
import ru.sendsay.sdk.models.EventType
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.SendsayProject
import ru.sendsay.sdk.network.SendsayService
import ru.sendsay.sdk.network.SendsayServiceImpl
import ru.sendsay.sdk.network.NetworkHandler
import ru.sendsay.sdk.network.NetworkHandlerImpl
import ru.sendsay.sdk.preferences.SendsayPreferences
import ru.sendsay.sdk.preferences.SendsayPreferencesImpl
import ru.sendsay.sdk.repository.AppInboxCache
import ru.sendsay.sdk.repository.AppInboxCacheImpl
import ru.sendsay.sdk.repository.CampaignRepository
import ru.sendsay.sdk.repository.CampaignRepositoryImpl
import ru.sendsay.sdk.repository.CustomerIdsRepository
import ru.sendsay.sdk.repository.CustomerIdsRepositoryImpl
import ru.sendsay.sdk.repository.DeviceInitiatedRepository
import ru.sendsay.sdk.repository.DeviceInitiatedRepositoryImpl
import ru.sendsay.sdk.repository.DrawableCacheImpl
import ru.sendsay.sdk.repository.EventRepository
import ru.sendsay.sdk.repository.EventRepositoryImpl
import ru.sendsay.sdk.repository.SendsayConfigRepository
import ru.sendsay.sdk.repository.FontCacheImpl
import ru.sendsay.sdk.repository.HtmlNormalizedCacheImpl
import ru.sendsay.sdk.repository.InAppContentBlockDisplayStateRepositoryImpl
import ru.sendsay.sdk.repository.InAppMessageDisplayStateRepositoryImpl
import ru.sendsay.sdk.repository.InAppMessagesCache
import ru.sendsay.sdk.repository.InAppMessagesCacheImpl
import ru.sendsay.sdk.repository.PushNotificationRepository
import ru.sendsay.sdk.repository.PushNotificationRepositoryImpl
import ru.sendsay.sdk.repository.PushTokenRepository
import ru.sendsay.sdk.repository.PushTokenRepositoryProvider
import ru.sendsay.sdk.repository.SegmentsCacheImpl
import ru.sendsay.sdk.repository.UniqueIdentifierRepository
import ru.sendsay.sdk.repository.UniqueIdentifierRepositoryImpl
import ru.sendsay.sdk.services.SendsayProjectFactory
import ru.sendsay.sdk.services.inappcontentblock.InAppContentBlockTrackingDelegateImpl
import ru.sendsay.sdk.util.SendsayGson
import ru.sendsay.sdk.util.TokenType
import ru.sendsay.sdk.util.currentTimeSeconds
import ru.sendsay.sdk.util.logOnException
import ru.sendsay.sdk.view.InAppMessagePresenter

internal class SendsayComponent(
    var sendsayConfiguration: SendsayConfiguration,
    context: Context
) {
    private val application = context.applicationContext

    // Preferences
    internal val preferences: SendsayPreferences = SendsayPreferencesImpl(context)

    internal val projectFactory = SendsayProjectFactory(
        context,
        sendsayConfiguration
    )

    // Repositories
    internal val deviceInitiatedRepository: DeviceInitiatedRepository = DeviceInitiatedRepositoryImpl(
            preferences
    )

    private val uniqueIdentifierRepository: UniqueIdentifierRepository = UniqueIdentifierRepositoryImpl(
            preferences
    )

    internal val customerIdsRepository: CustomerIdsRepository = CustomerIdsRepositoryImpl(
            SendsayGson.instance, uniqueIdentifierRepository, preferences
    )

    internal val pushNotificationRepository: PushNotificationRepository = PushNotificationRepositoryImpl(
            preferences
    )

    internal val eventRepository: EventRepository = EventRepositoryImpl(context)

    internal val pushTokenRepository: PushTokenRepository = PushTokenRepositoryProvider.get(context)

    internal val campaignRepository: CampaignRepository = CampaignRepositoryImpl(SendsayGson.instance, preferences)

    internal val inAppMessagesCache: InAppMessagesCache = InAppMessagesCacheImpl(context, SendsayGson.instance)

    internal val appInboxCache: AppInboxCache = AppInboxCacheImpl(context, SendsayGson.instance)

    internal val inAppMessageDisplayStateRepository =
        InAppMessageDisplayStateRepositoryImpl(preferences, SendsayGson.instance)

    // Network Handler
    internal val networkManager: NetworkHandler = NetworkHandlerImpl(sendsayConfiguration)

    // Api Service
    internal val sendsayService: SendsayService = SendsayServiceImpl(SendsayGson.instance, networkManager)

    // Managers
    internal val fetchManager: FetchManager = FetchManagerImpl(sendsayService, SendsayGson.instance)

    internal val backgroundTimerManager: BackgroundTimerManager = BackgroundTimerManagerImpl(
        context, sendsayConfiguration
    )

    internal val serviceManager: ServiceManager = ServiceManagerImpl()

    internal val connectionManager: ConnectionManager = ConnectionManagerImpl(context)

    internal val fontCache: FontCache = FontCacheImpl(context)

    internal val drawableCache = DrawableCacheImpl(context)

    internal val inAppMessagePresenter = InAppMessagePresenter(context, drawableCache)

    internal val segmentsCache = SegmentsCacheImpl(context, SendsayGson.instance)

    internal val segmentsManager: SegmentsManager = SegmentsManagerImpl(
        fetchManager = fetchManager,
        projectFactory = projectFactory,
        customerIdsRepository = customerIdsRepository,
        segmentsCache = segmentsCache
    )

    internal val flushManager: FlushManager = FlushManagerImpl(
        sendsayConfiguration,
        eventRepository,
        sendsayService,
        connectionManager,
        onEventUploaded = { uploadedEvent ->
            inAppMessageManager?.onEventUploaded(uploadedEvent)
            segmentsManager.onEventUploaded(uploadedEvent)
        }
    )

    internal val eventManager: EventManager = EventManagerImpl(
        sendsayConfiguration, eventRepository, customerIdsRepository, flushManager, projectFactory,
        onEventCreated = { event, type ->
            inAppMessageManager?.onEventCreated(event, type)
            appInboxManager?.onEventCreated(event, type)
            inAppContentBlockManager?.onEventCreated(event, type)
        }
    )

    internal val campaignManager: CampaignManager = CampaignManagerImpl(
        campaignRepository, eventManager
    )

    internal val inAppMessageTrackingDelegate: InAppMessageTrackingDelegate = EventManagerInAppMessageTrackingDelegate(
        context, eventManager
    )

    internal val inAppContentBlockTrackingDelegate = InAppContentBlockTrackingDelegateImpl(
        context, eventManager
    )

    internal val trackingConsentManager: TrackingConsentManager = TrackingConsentManagerImpl(
        eventManager, campaignRepository, inAppMessageTrackingDelegate, inAppContentBlockTrackingDelegate
    )

    internal val fcmManager: FcmManager = FcmManagerImpl(
        context,
        sendsayConfiguration,
        eventManager,
        pushTokenRepository,
        trackingConsentManager
    )

    internal val sessionManager: SessionManager = SessionManagerImpl(
        context,
        preferences,
        campaignRepository,
        eventManager,
        backgroundTimerManager,
        sendsayConfiguration.manualSessionAutoClose
    )

    internal val initConfigManager: InitConfigManager = InitConfigManagerImpl(
        fetchManager,
        projectFactory
    )

    internal val pushNotificationSelfCheckManager: PushNotificationSelfCheckManager =
        PushNotificationSelfCheckManagerImpl(
            context,
            sendsayConfiguration,
            customerIdsRepository,
            pushTokenRepository,
            flushManager,
            sendsayService,
            projectFactory
        )

    internal val appInboxManager: AppInboxManager? = if (Sendsay.isAppInboxEnabled) AppInboxManagerImpl(
        fetchManager = fetchManager,
        drawableCache = drawableCache,
        projectFactory = projectFactory,
        customerIdsRepository = customerIdsRepository,
        appInboxCache = appInboxCache
    ) else null

    internal val inAppMessageManager: InAppMessageManager? = if (Sendsay.isInAppMessagesEnabled) InAppMessageManagerImpl(
        customerIdsRepository,
        inAppMessagesCache,
        fetchManager,
        inAppMessageDisplayStateRepository,
        drawableCache,
        fontCache,
        inAppMessagePresenter,
        trackingConsentManager,
        projectFactory
    ) else null

    internal val inAppContentBlockDisplayStateRepository = InAppContentBlockDisplayStateRepositoryImpl(
        preferences
    )

    internal val htmlNormalizedCache = HtmlNormalizedCacheImpl(
        context,
        preferences
    )

    internal val inAppContentBlockManager: InAppContentBlockManager? = if (Sendsay.isInAppCBEnabled)  InAppContentBlockManagerImpl(
        inAppContentBlockDisplayStateRepository,
        fetchManager,
        projectFactory,
        customerIdsRepository,
        drawableCache,
        htmlNormalizedCache,
        fontCache
    ) else null

    fun anonymize(
        sendsayProject: SendsayProject,
        projectRouteMap: Map<EventType, List<SendsayProject>> = hashMapOf()
    ) {
        if (sendsayConfiguration.automaticSessionTracking) {
            sessionManager.trackSessionEnd(currentTimeSeconds())
        }

        val token = pushTokenRepository.get()
        val tokenType = pushTokenRepository.getLastTokenType()
        // Do not use TokenFrequency from the configuration, clear tokens immediately during anonymize
        fcmManager.trackToken(" ", SendsayConfiguration.TokenFrequency.EVERY_LAUNCH, TokenType.FCM)
        fcmManager.trackToken(" ", SendsayConfiguration.TokenFrequency.EVERY_LAUNCH, TokenType.HMS)
        deviceInitiatedRepository.set(false)
        campaignRepository.clear()
        inAppMessageManager?.clear()
        uniqueIdentifierRepository.clear()
        customerIdsRepository.clear()
        inAppContentBlockManager?.clearAll()
        sessionManager.reset()
        segmentsManager.clearAll()
        pushNotificationRepository.clearAll()
        fontCache.clear()
        drawableCache.clear()

        sendsayConfiguration.baseURL = sendsayProject.baseUrl
        sendsayConfiguration.projectToken = sendsayProject.projectToken
        sendsayConfiguration.authorization = sendsayProject.authorization
        sendsayConfiguration.inAppContentBlockPlaceholdersAutoLoad =
            sendsayProject.inAppContentBlockPlaceholdersAutoLoad
        sendsayConfiguration.projectRouteMap = projectRouteMap
        SendsayConfigRepository.set(application, sendsayConfiguration)
        runCatching {
            projectFactory.reset(sendsayConfiguration)
            // Advanced auth could be invalid while reset.
            // We cannot throw exception directly, it will be catch-ed anyway without runCatching
            // but we need to complete anonymization process
            // Exception will be still re-thrown for `safeModeEnabled` == false
        }.logOnException()

        Sendsay.trackInstallEvent()
        if (sendsayConfiguration.automaticSessionTracking) {
            sessionManager.trackSessionStart(currentTimeSeconds())
        }
        // Do not use TokenFrequency from the configuration, setup token from new customer immediately during anonymize
        fcmManager.trackToken(token, SendsayConfiguration.TokenFrequency.EVERY_LAUNCH, tokenType)
        inAppMessageManager?.reload()
        appInboxManager?.reload()
        inAppContentBlockManager?.loadInAppContentBlockPlaceholders()
    }
}
