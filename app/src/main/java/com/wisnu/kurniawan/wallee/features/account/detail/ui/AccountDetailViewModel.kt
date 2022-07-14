package com.wisnu.kurniawan.wallee.features.account.detail.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.wisnu.kurniawan.wallee.features.account.detail.data.IAccountDetailEnvironment
import com.wisnu.kurniawan.wallee.features.balance.summary.data.AccountBalance
import com.wisnu.kurniawan.wallee.foundation.extension.formatAsBigDecimal
import com.wisnu.kurniawan.wallee.foundation.extension.formatAsDecimal
import com.wisnu.kurniawan.wallee.foundation.extension.formatAsDisplayNormalize
import com.wisnu.kurniawan.wallee.foundation.extension.isDecimalNotExceed
import com.wisnu.kurniawan.wallee.foundation.extension.toggleFormatDisplay
import com.wisnu.kurniawan.wallee.foundation.viewmodel.StatefulViewModel
import com.wisnu.kurniawan.wallee.model.AccountType
import com.wisnu.kurniawan.wallee.runtime.navigation.ARG_ACCOUNT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    accountDetailEnvironment: IAccountDetailEnvironment
) : StatefulViewModel<AccountDetailState, AccountDetailEffect, AccountDetailAction, IAccountDetailEnvironment>(AccountDetailState(), accountDetailEnvironment) {

    private val accountId = savedStateHandle.get<String>(ARG_ACCOUNT_ID).orEmpty()

    init {
        viewModelScope.launch {
            if (accountId.isNotBlank()) {
                environment.getAccount(accountId)
                    .collect {
                        setState {
                            val name = it.name
                            val totalAmount = it.currency.formatAsDisplayNormalize(it.amount, false)
                            copy(
                                accountTypeItems = initialAccountTypeItems(it.type),
                                name = TextFieldValue(name, TextRange(name.length)),
                                amountItem = AmountItem(
                                    totalAmount = TextFieldValue(totalAmount, TextRange(totalAmount.length)),
                                    isEditable = false
                                ),
                                currency = it.currency,
                                createdAt = it.createdAt
                            )
                        }
                    }
            } else {
                setState { copy(accountTypeItems = initialAccountTypeItems()) }
            }
        }
    }

    override fun dispatch(action: AccountDetailAction) {
        when (action) {
            is AccountDetailAction.NameChange -> {
                viewModelScope.launch {
                    setState { copy(name = action.name) }
                }
            }
            AccountDetailAction.Save -> {
                viewModelScope.launch {
                    try {
                        val account = AccountBalance(
                            id = accountId,
                            currency = state.value.currency,
                            amount = state.value.amountItem.totalAmount.formatAsBigDecimal(),
                            name = state.value.name.text.trim(),
                            type = state.value.selectedAccountType(),
                            createdAt = state.value.createdAt
                        )
                        environment.saveAccount(
                            account
                        )
                        setState { copy(shouldShowDuplicateNameError = false) }
                        setEffect(AccountDetailEffect.ClosePage)
                    } catch (e: Exception) {
                        setState { copy(shouldShowDuplicateNameError = true) }
                    }
                }
            }
            is AccountDetailAction.SelectAccountType -> {
                viewModelScope.launch {
                    setState { copy(accountTypeItems = accountTypeItems.select(action.selectedAccountType)) }
                }
            }
            is AccountDetailAction.TotalAmountAction.Change -> {
                viewModelScope.launch {
                    runCatching {
                        action.totalAmount.formatAsDecimal().apply {
                            if (this.isDecimalNotExceed()) {
                                setState { copy(amountItem = amountItem.copy(totalAmount = this@apply)) }
                            }
                        }
                    }
                }
            }
            is AccountDetailAction.TotalAmountAction.FocusChange -> {
                viewModelScope.launch {
                    val totalAmountText = state.value.amountItem.totalAmount.text
                    val totalAmountFormatted = state.value.currency.toggleFormatDisplay(!action.isFocused, totalAmountText)
                    setState { copy(amountItem = amountItem.copy(totalAmount = amountItem.totalAmount.copy(text = totalAmountFormatted))) }
                }
            }
        }
    }

    private fun initialAccountTypeItems(selectedAccountType: AccountType = AccountType.CASH): List<AccountTypeItem> {
        return listOf(
            AccountType.CASH,
            AccountType.BANK,
            AccountType.INVESTMENT,
            AccountType.E_WALLET,
            AccountType.OTHERS
        ).map {
            AccountTypeItem(it, selected = it == selectedAccountType)
        }
    }

}
