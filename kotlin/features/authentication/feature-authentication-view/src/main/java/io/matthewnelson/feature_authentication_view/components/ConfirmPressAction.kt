package io.matthewnelson.feature_authentication_view.components

import app.cash.exhaustive.Exhaustive
import io.matthewnelson.feature_authentication_core.model.AuthenticateFlowResponse.ConfirmPinEntryToSetForFirstTime
import io.matthewnelson.feature_authentication_core.model.AuthenticateFlowResponse.ConfirmNewPinEntryToReset

internal class ConfirmPressAction {

    @Volatile
    private var action: Action = Action.Authenticate

    @JvmSynthetic
    @Synchronized
    fun getAction(): Action =
        action

    @JvmSynthetic
    @Synchronized
    fun updateAction(newAction: Action) {
        action.let { currentAction ->
            @Exhaustive
            when (currentAction) {
                is Action.Authenticate -> {}
                is Action.ResetPin -> {
                    currentAction.flowResponseResetPin.clearCurrentValidPinEntry()
                    currentAction.flowResponseResetPin.clearNewPinEntry()
                }
                is Action.SetPinFirstTime -> {
                    currentAction.flowResponseSetPinFirstTime.clearInitialPinEntry()
                }
            }
        }
        action = newAction
    }

    sealed class Action {
        object Authenticate : Action()

        class ResetPin private constructor(
            val flowResponseResetPin: ConfirmNewPinEntryToReset
        ): Action() {
            companion object {
                @JvmSynthetic
                fun instantiate(flowResponseResetPin: ConfirmNewPinEntryToReset): ResetPin =
                    ResetPin(flowResponseResetPin)
            }
        }

        class SetPinFirstTime private constructor(
            val flowResponseSetPinFirstTime: ConfirmPinEntryToSetForFirstTime
        ): Action() {
            companion object {
                @JvmSynthetic
                fun instantiate(
                    flowResponseSetPinFirstTime: ConfirmPinEntryToSetForFirstTime
                ): SetPinFirstTime =
                    SetPinFirstTime(flowResponseSetPinFirstTime)
            }
        }
    }
}
