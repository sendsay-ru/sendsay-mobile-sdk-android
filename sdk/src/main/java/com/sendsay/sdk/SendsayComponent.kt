package com.sendsay.sdk

import android.content.Context
import com.sendsay.sdk.repository.FontCache
import com.sendsay.sdk.manager.AppInboxManager
import com.sendsay.sdk.manager.AppInboxManagerImpl
import com.sendsay.sdk.manager.BackgroundTimerManager
import com.sendsay.sdk.manager.BackgroundTimerManagerImpl
import com.sendsay.sdk.manager.CampaignManager
import com.sendsay.sdk.manager.CampaignManagerImpl
import com.sendsay.sdk.manager.ConnectionManager
import com.sendsay.sdk.manager.ConnectionManagerImpl
import com.sendsay.sdk.manager.EventManager
import com.sendsay.sdk.manager.EventManagerImpl
import com.sendsay.sdk.manager.FcmManager
import com.sendsay.sdk.manager.FcmManagerImpl
import com.sendsay.sdk.manager.FetchManager
import com.sendsay.sdk.manager.FetchManagerImpl
import com.sendsay.sdk.manager.FlushManager
import com.sendsay.sdk.manager.FlushManagerImpl
import com.sendsay.sdk.manager.PushNotificationSelfCheckManager
import com.sendsay.sdk.manager.PushNotificationSelfCheckManagerImpl
import com.sendsay.sdk.manager.SegmentsManager
import com.sendsay.sdk.manager.SegmentsManagerImpl
import com.sendsay.sdk.manager.ServiceManager
import com.sendsay.sdk.manager.ServiceManagerImpl
import com.sendsay.sdk.manager.SessionManager
import com.sendsay.sdk.manager.SessionManagerImpl
import com.sendsay.sdk.manager.TrackingConsentManager
import com.sendsay.sdk.manager.TrackingConsentManagerImpl
import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.SendsayProject
import com.sendsay.sdk.network.SendsayService
import com.sendsay.sdk.network.SendsayServiceImpl
import com.sendsay.sdk.network.NetworkHandler
import com.sendsay.sdk.network.NetworkHandlerImpl
import com.sendsay.sdk.preferences.SendsayPreferences
import com.sendsay.sdk.preferences.SendsayPreferencesImpl
import com.sendsay.sdk.repository.AppInboxCache
import com.sendsay.sdk.repository.AppInboxCacheImpl
import com.sendsay.sdk.repository.CampaignRepository
import com.sendsay.sdk.repository.CampaignRepositoryImpl
import com.sendsay.sdk.repository.CustomerIdsRepository
import com.sendsay.sdk.repository.CustomerIdsRepositoryImpl
import com.sendsay.sdk.repository.DeviceInitiatedRepository
import com.sendsay.sdk.repository.DeviceInitiatedRepositoryImpl
import com.sendsay.sdk.repository.DrawableCacheImpl
import com.sendsay.sdk.repository.EventRepository
import com.sendsay.sdk.repository.EventRepositoryImpl
import com.sendsay.sdk.repository.SendsayConfigRepository
import com.sendsay.sdk.repository.FontCacheImpl
import com.sendsay.sdk.repository.HtmlNormalizedCacheImpl
import com.sendsay.sdk.repository.InAppContentBlockDisplayStateRepositoryImpl
import com.sendsay.sdk.repository.PushNotificationRepository
import com.sendsay.sdk.repository.PushNotificationRepositoryImpl
import com.sendsay.sdk.repository.PushTokenRepository
import com.sendsay.sdk.repository.PushTokenRepositoryProvider
import com.sendsay.sdk.repository.SegmentsCacheImpl
import com.sendsay.sdk.repository.UniqueIdentifierRepository
import com.sendsay.sdk.repository.UniqueIdentifierRepositoryImpl
import com.sendsay.sdk.services.SendsayProjectFactory
import com.sendsay.sdk.services.inappcontentblock.InAppContentBlockTrackingDelegateImpl
import com.sendsay.sdk.util.SendsayGson
import com.sendsay.sdk.util.TokenType
import com.sendsay.sdk.util.currentTimeSeconds
import com.sendsay.sdk.util.logOnException
import com.sendsay.sdk.view.InAppMessagePresenter

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

//    internal val inAppMessagesCache: InAppMessagesCache = InAppMessagesCacheImpl(context, SendsayGson.instance)

    internal val appInboxCache: AppInboxCache = AppInboxCacheImpl(context, SendsayGson.instance)

//    internal val inAppMessageDisplayStateRepository =
//        InAppMessageDisplayStateRepositoryImpl(preferences, SendsayGson.instance)

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
//            inAppMessageManager.onEventUploaded(uploadedEvent)
            segmentsManager.onEventUploaded(uploadedEvent)
        }
    )

    internal val eventManager: EventManager = EventManagerImpl(
        sendsayConfiguration, eventRepository, customerIdsRepository, flushManager, projectFactory,
        onEventCreated = { event, type ->
//            inAppMessageManager.onEventCreated(event, type)
            appInboxManager.onEventCreated(event, type)
//            inAppContentBlockManager.onEventCreated(event, type)
        }
    )

    internal val campaignManager: CampaignManager = CampaignManagerImpl(
        campaignRepository, eventManager
    )

//    internal val inAppMessageTrackingDelegate: InAppMessageTrackingDelegate = EventManagerInAppMessageTrackingDelegate(
//        context, eventManager
//    )

    internal val inAppContentBlockTrackingDelegate = InAppContentBlockTrackingDelegateImpl(
        context, eventManager
    )

    internal val trackingConsentManager: TrackingConsentManager = TrackingConsentManagerImpl(
        eventManager, campaignRepository, /*inAppMessageTrackingDelegate,*/ inAppContentBlockTrackingDelegate
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

    internal val appInboxManager: AppInboxManager = AppInboxManagerImpl(
        fetchManager = fetchManager,
        drawableCache = drawableCache,
        projectFactory = projectFactory,
        customerIdsRepository = customerIdsRepository,
        appInboxCache = appInboxCache
    )

//    internal val inAppMessageManager: InAppMessageManager = InAppMessageManagerImpl(
//        customerIdsRepository,
//        inAppMessagesCache,
//        fetchManager,
//        inAppMessageDisplayStateRepository,
//        inAppMessagesBitmapCache,
//        fontCache,
//        inAppMessagePresenter,
//        trackingConsentManager,
//        projectFactory
//    )

    internal val inAppContentBlockDisplayStateRepository = InAppContentBlockDisplayStateRepositoryImpl(
        preferences
    )

    internal val htmlNormalizedCache = HtmlNormalizedCacheImpl(
        context,
        preferences
    )

//    internal val inAppContentBlockManager: InAppContentBlockManager = InAppContentBlockManagerImpl(
//        inAppContentBlockDisplayStateRepository,
//        fetchManager,
//        projectFactory,
//        customerIdsRepository,
//        drawableCache,
//        htmlNormalizedCache,
//        fontCache
//    )

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
//        inAppMessageManager.clear()
        uniqueIdentifierRepository.clear()
        customerIdsRepository.clear()
//        inAppContentBlockManager.clearAll()
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
//        inAppMessageManager.reload()
        appInboxManager.reload()
//        inAppContentBlockManager.loadInAppContentBlockPlaceholders()
    }
}
