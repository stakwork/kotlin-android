package io.matthewnelson.concept_authentication_core.model

interface ConfirmUserInputToReset<U: UserInput<U>> {
    fun clearCurrentValidPinEntry()
    fun clearNewPinEntry()
    fun setNewPinEntry(newPinEntry: U?)
}

interface ConfirmUserInputToSetForFirstTime<U: UserInput<U>> {
    fun clearInitialPinEntry()
}