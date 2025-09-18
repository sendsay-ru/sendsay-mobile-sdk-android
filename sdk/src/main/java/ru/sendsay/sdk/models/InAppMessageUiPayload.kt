package ru.sendsay.sdk.models

import android.graphics.drawable.Drawable
import ru.sendsay.sdk.style.ContainerStyle
import ru.sendsay.sdk.style.InAppButtonStyle
import ru.sendsay.sdk.style.InAppCloseButtonStyle
import ru.sendsay.sdk.style.InAppImageStyle
import ru.sendsay.sdk.style.InAppLabelStyle

internal data class InAppMessageUiPayload(
    val image: ImageUiPayload,
    val title: TextUiPayload,
    val content: TextUiPayload,
    val closeButton: CloseButtonUiPayload,
    val buttons: List<ButtonUiPayload>,
    val container: ContainerStyle
)

internal data class CloseButtonUiPayload(
    val icon: Drawable,
    val style: InAppCloseButtonStyle
)

internal data class ButtonUiPayload(
    val text: String,
    val style: InAppButtonStyle,
    val originPayload: InAppMessagePayloadButton
)

internal data class TextUiPayload(
    val value: String?,
    val style: InAppLabelStyle
)

internal data class ImageUiPayload(
    val source: Drawable?,
    val style: InAppImageStyle
)
