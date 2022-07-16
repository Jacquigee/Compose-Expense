package com.wisnu.kurniawan.wallee.features.account.detail.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisnu.kurniawan.wallee.R
import com.wisnu.kurniawan.wallee.foundation.extension.getLabel
import com.wisnu.kurniawan.wallee.foundation.extension.getSymbol
import com.wisnu.kurniawan.wallee.foundation.theme.AlphaDisabled
import com.wisnu.kurniawan.wallee.foundation.theme.AlphaHigh
import com.wisnu.kurniawan.wallee.foundation.theme.MediumRadius
import com.wisnu.kurniawan.wallee.foundation.uicomponent.ActionContentCell
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgBasicTextField
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgContentTitle
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgErrorLabel
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgHeaderEditMode
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgHeadlineLabel
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgIcon
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgPageLayout
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgSecondaryButton
import com.wisnu.kurniawan.wallee.foundation.uiextension.collectAsEffectWithLifecycle
import com.wisnu.kurniawan.wallee.foundation.uiextension.paddingCell

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun AccountDetailScreen(
    viewModel: AccountDetailViewModel,
    onClosePage: () -> Unit,
    onCancelClick: () -> Unit,
    onCategorySectionClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsEffectWithLifecycle()

    val localFocusManager = LocalFocusManager.current

    when (effect) {
        AccountDetailEffect.ClosePage -> {
            LaunchedEffect(effect) {
                onClosePage()
            }
        }
        null -> {}
    }

    AccountDetailScreen(
        state = state,
        onSaveClick = {
            localFocusManager.clearFocus()
            viewModel.dispatch(AccountDetailAction.Save)
        },
        onCancelClick = {
            localFocusManager.clearFocus()
            onCancelClick()
        },
        onDeleteClick = {
            localFocusManager.clearFocus()
            viewModel.dispatch(AccountDetailAction.Delete)
        },
        onCategorySectionClick = {
            localFocusManager.clearFocus()
            onCategorySectionClick()
        },
        onTotalAmountChange = { viewModel.dispatch(AccountDetailAction.TotalAmountAction.Change(it)) },
        onTotalAmountFocusChange = { viewModel.dispatch(AccountDetailAction.TotalAmountAction.FocusChange(it)) },
        onNameChange = { viewModel.dispatch(AccountDetailAction.NameChange(it)) },
    )
}

@Composable
private fun AccountDetailScreen(
    state: AccountDetailState,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onNameChange: (TextFieldValue) -> Unit,
    onCategorySectionClick: () -> Unit,
    onTotalAmountChange: (TextFieldValue) -> Unit,
    onTotalAmountFocusChange: (Boolean) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current
    PgPageLayout(
        Modifier.fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        localFocusManager.clearFocus()
                    }
                )
            }
    ) {
        PgHeaderEditMode(
            isAllowToSave = state.isValid(),
            title = state.getTitle(),
            onSaveClick = onSaveClick,
            onCancelClick = onCancelClick,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            item {
                if (state.shouldShowDuplicateNameError) {
                    PgErrorLabel(
                        text = stringResource(R.string.account_edit_name_duplicate),
                        modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
                    )
                }

                NameSection(
                    name = state.name,
                    onNameChange = onNameChange,
                    isError = state.shouldShowDuplicateNameError
                )

                CategorySection(
                    categoryName = stringResource(state.selectedAccountType().getLabel()),
                    onCategorySectionClick = onCategorySectionClick
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            item {
                AmountSection(
                    totalAmount = state.totalAmount,
                    amountSymbol = state.currency.getSymbol() + " ",
                    enabled = !state.isEditMode,
                    onTotalAmountChange = onTotalAmountChange,
                    onTotalAmountFocusChange = onTotalAmountFocusChange
                )
            }

            if (state.canDelete()) {
                item {
                    Spacer(Modifier.height(32.dp))
                    PgSecondaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error
                        ),
                        onClick = onDeleteClick
                    ) {
                        PgContentTitle(text = stringResource(R.string.account_edit_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}


@Composable
private fun NameSection(
    name: TextFieldValue,
    isError: Boolean,
    onNameChange: (TextFieldValue) -> Unit,
) {
    val titleColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onBackground
    }
    ActionContentCell(
        title = stringResource(R.string.account_edit_name),
        titleColor = titleColor,
        showDivider = true,
        insetSize = 70.dp,
        shape = RoundedCornerShape(
            topStart = MediumRadius,
            topEnd = MediumRadius
        ),
        trailing = {
            val focusManager = LocalFocusManager.current
            PgBasicTextField(
                value = name,
                onValueChange = onNameChange,
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                placeholderValue = stringResource(R.string.account_edit_name_hint),
                singleLine = true
            )
        },
    )
}

@Composable
private fun CategorySection(
    categoryName: String,
    onCategorySectionClick: () -> Unit,
) {
    ActionContentCell(
        title = stringResource(R.string.category),
        showDivider = false,
        shape = RoundedCornerShape(
            bottomStart = MediumRadius,
            bottomEnd = MediumRadius
        ),
        insetSize = 70.dp,
        onClick = onCategorySectionClick,
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                PgContentTitle(
                    text = categoryName,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                PgIcon(
                    imageVector = Icons.Rounded.ChevronRight,
                    tint = LocalContentColor.current.copy(alpha = AlphaDisabled)
                )
            }
        },
    )
}

@Composable
private fun AmountSection(
    totalAmount: TextFieldValue,
    amountSymbol: String,
    enabled: Boolean,
    onTotalAmountChange: (TextFieldValue) -> Unit,
    onTotalAmountFocusChange: (Boolean) -> Unit,
) {
    PgHeadlineLabel(
        text = stringResource(R.string.account_edit_balance),
        modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
    )

    Row(
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.secondary,
            shape = MaterialTheme.shapes.medium
        )
            .fillMaxWidth()
            .paddingCell()
    ) {
        PgContentTitle(
            text = amountSymbol,
            color = MaterialTheme.colorScheme.onBackground.copy(
                alpha = if (enabled) {
                    AlphaHigh
                } else {
                    AlphaDisabled
                }
            )
        )
        val localFocusManager = LocalFocusManager.current
        PgBasicTextField(
            value = totalAmount,
            onValueChange = onTotalAmountChange,
            modifier = Modifier.onFocusChanged {
                if (enabled) {
                    onTotalAmountFocusChange(it.isFocused)
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            singleLine = true,
            keyboardActions = KeyboardActions(
                onDone = {
                    localFocusManager.clearFocus()
                }
            ),
            enabled = enabled
        )
    }
}
