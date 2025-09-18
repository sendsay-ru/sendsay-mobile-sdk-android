package ru.sendsay.sdk.manager

import android.content.Context
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.exceptions.InvalidConfigurationException
import ru.sendsay.sdk.models.CampaignData
import ru.sendsay.sdk.models.Constants
import ru.sendsay.sdk.models.EventType
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.network.SendsayServiceImpl
import ru.sendsay.sdk.network.NetworkHandlerImpl
import ru.sendsay.sdk.preferences.SendsayPreferencesImpl
import ru.sendsay.sdk.repository.CampaignRepository
import ru.sendsay.sdk.repository.CampaignRepositoryImpl
import ru.sendsay.sdk.repository.CustomerIdsRepositoryImpl
import ru.sendsay.sdk.repository.UniqueIdentifierRepositoryImpl
import ru.sendsay.sdk.services.SendsayProjectFactory
import ru.sendsay.sdk.util.SendsayGson
import ru.sendsay.sdk.util.Logger

internal class CampaignManagerImpl(
    private val campaignRepository: CampaignRepository,
    private val eventManager: EventManager
) : CampaignManager {

    companion object {
        fun createSdklessInstance(context: Context, configuration: SendsayConfiguration): CampaignManagerImpl {
            val preferences = SendsayPreferencesImpl(context)
            val campaignRepository = CampaignRepositoryImpl(SendsayGson.instance, preferences)
            val eventRepository = TemporaryEventRepositoryImpl(context)
            val uniqueIdentifierRepository = UniqueIdentifierRepositoryImpl(preferences)
            val customerIdsRepository = CustomerIdsRepositoryImpl(
                SendsayGson.instance, uniqueIdentifierRepository, preferences
            )
            val networkManager = NetworkHandlerImpl(configuration)
            val sendsayService = SendsayServiceImpl(SendsayGson.instance, networkManager)
            val connectionManager = ConnectionManagerImpl(context)
            val flushManager = FlushManagerImpl(
                configuration,
                eventRepository,
                sendsayService,
                connectionManager,
                {
                    // no action for identifyCustomer - SDK is not initialized
                }
            )
            val projectFactory = try {
                SendsayProjectFactory(context, configuration)
            } catch (e: InvalidConfigurationException) {
                if (configuration.advancedAuthEnabled) {
                    Logger.w(this, "Turning off advanced auth for campaign data tracking")
                    configuration.advancedAuthEnabled = false
                }
                SendsayProjectFactory(context, configuration)
            }
            val eventManager = EventManagerImpl(
                configuration, eventRepository, customerIdsRepository, flushManager, projectFactory,
                onEventCreated = { event, type ->
                    // no action for any event - SDK is not initialized
                }
            )
            return CampaignManagerImpl(campaignRepository, eventManager)
        }
    }

    override fun trackCampaignClick(campaignData: CampaignData): Boolean {
        if (Sendsay.isStopped) {
            Logger.e(this, "Campaign event not tracked, SDK is stopping")
            return false
        }
        // store campaign data to be used by session_start event
        campaignRepository.set(campaignData)
        // but stop tracking campaign click if is not valid
        if (!campaignData.isValidForCampaignTrack()) {
            Logger.v(this, "Campaign click not tracked because ${campaignData.completeUrl} is invalid")
            return false
        }
        val properties = HashMap<String, Any>()
        properties["platform"] = "Android"
        properties["timestamp"] = campaignData.createdAt
        properties.putAll(campaignData.getTrackingData())
        eventManager.track(
            eventType = Constants.EventTypes.push,
            properties = properties,
            type = EventType.CAMPAIGN_CLICK
        )
        return true
    }
}
