package com.wisnu.kurniawan.wallee.features.transaction.detail.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wisnu.kurniawan.wallee.R
import com.wisnu.kurniawan.wallee.foundation.extension.getEmojiAndText
import com.wisnu.kurniawan.wallee.foundation.extension.getLabel
import com.wisnu.kurniawan.wallee.foundation.theme.AlphaDisabled
import com.wisnu.kurniawan.wallee.foundation.theme.MediumRadius
import com.wisnu.kurniawan.wallee.foundation.uicomponent.ActionContentCell
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgBasicTextField
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgContentTitle
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgHeaderEditMode
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgHeadlineLabel
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgIcon
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgPageLayout
import com.wisnu.kurniawan.wallee.foundation.uicomponent.PgTabLabel
import com.wisnu.kurniawan.wallee.foundation.uiextension.collectAsEffectWithLifecycle
import com.wisnu.kurniawan.wallee.foundation.uiextension.paddingCell
import com.wisnu.kurniawan.wallee.foundation.uiextension.showDatePicker
import com.wisnu.kurniawan.wallee.model.AccountType
import com.wisnu.kurniawan.wallee.model.CategoryType
import com.wisnu.kurniawan.wallee.model.TransactionType
import com.wisnu.kurniawan.wallee.runtime.navigation.TransactionDetailFlow

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun TransactionDetailScreen(
    navController: NavController,
    viewModel: TransactionDetailViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsEffectWithLifecycle()

    val localFocusManager = LocalFocusManager.current
    val activity = LocalContext.current as AppCompatActivity

    when (effect) {
        TransactionEffect.ClosePage -> {
            LaunchedEffect(effect) {
                navController.navigateUp()
            }
        }
        null -> {}
    }

    TransactionDetailScreen(
        state = state,
        onSaveClick = {
            localFocusManager.clearFocus()
            viewModel.dispatch(TransactionAction.Save)
        },
        onCancelClick = {
            localFocusManager.clearFocus()
            navController.navigateUp()
        },
        onAccountSectionClick = {
            localFocusManager.clearFocus()
            navController.navigate(TransactionDetailFlow.SelectAccount.route)
        },
        onCategorySectionClick = {
            localFocusManager.clearFocus()
            navController.navigate(TransactionDetailFlow.SelectCategory.route)
        },
        onTransferAccountSectionClick = {
            localFocusManager.clearFocus()
            navController.navigate(TransactionDetailFlow.SelectTransferAccount.route)
        },
        onDateSectionClick = {
            activity.showDatePicker(state.transactionDate.toLocalDate()) { selectedDate ->
                viewModel.dispatch(TransactionAction.SelectDate(selectedDate))
            }
        },
        onTransactionTypeSelected = {
            localFocusManager.clearFocus()
            viewModel.dispatch(TransactionAction.SelectTransactionType(it))
        },
        onTotalAmountChange = { viewModel.dispatch(TransactionAction.TotalAmountAction.Change(it)) },
        onTotalAmountFocusChange = { viewModel.dispatch(TransactionAction.TotalAmountAction.FocusChange(it)) },
        onNoteChange = { viewModel.dispatch(TransactionAction.ChangeNote(it)) },
    )
}

@Composable
private fun TransactionDetailScreen(
    state: TransactionState,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onAccountSectionClick: () -> Unit,
    onCategorySectionClick: () -> Unit,
    onTransferAccountSectionClick: () -> Unit,
    onDateSectionClick: () -> Unit,
    onTransactionTypeSelected: (TransactionTypeItem) -> Unit,
    onTotalAmountChange: (TextFieldValue) -> Unit,
    onTotalAmountFocusChange: (Boolean) -> Unit,
    onNoteChange: (TextFieldValue) -> Unit,
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
            title = stringResource(R.string.transaction_edit_add),
            onSaveClick = onSaveClick,
            onCancelClick = onCancelClick,
        )

        TransactionTypeSection(
            transactionTypes = state.transactionTypeItems,
            onSelected = onTransactionTypeSelected
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            item {
                AmountSection(
                    totalAmount = state.totalAmount,
                    totalAmountDisplay = state.getCurrencySymbol() + " ",
                    amountColor = state.getAmountColor(MaterialTheme.colorScheme.onSurface),
                    onTotalAmountChange = onTotalAmountChange,
                    onTotalAmountFocusChange = onTotalAmountFocusChange
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            item {
                GeneralSection(
                    transactionType = state.selectedTransactionType(),
                    selectedAccount = state.selectedAccountName() ?: stringResource(AccountType.CASH.getLabel()),
                    selectedCategoryType = state.selectedCategoryType(),
                    selectedTransferAccount = state.selectedAccountTransferName(),
                    transactionDate = state.transactionDateDisplayable(),
                    onAccountSectionClick = onAccountSectionClick,
                    onCategorySectionClick = onCategorySectionClick,
                    onTransferAccountSectionClick = onTransferAccountSectionClick,
                    onDateSectionClick = onDateSectionClick
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            item {
                NoteSection(
                    note = state.note,
                    hint = stringResource(state.noteHintDisplayable()),
                    onNoteChange = onNoteChange
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TransactionTypeSection(
    transactionTypes: List<TransactionTypeItem>,
    onSelected: (TransactionTypeItem) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(transactionTypes.size),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = MaterialTheme.shapes.extraLarge
                )
        ) {
            items(transactionTypes) {
                val backgroundColor = if (it.selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }
                val contentColor = if (it.selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondary
                }

                Chip(
                    onClick = { onSelected(it) },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = backgroundColor,
                        contentColor = contentColor
                    )
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        CompositionLocalProvider(LocalContentColor provides contentColor) {
                            PgTabLabel(stringResource(it.title))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountSection(
    totalAmount: TextFieldValue,
    totalAmountDisplay: String,
    amountColor: Color,
    onTotalAmountChange: (TextFieldValue) -> Unit,
    onTotalAmountFocusChange: (Boolean) -> Unit,
) {
    PgHeadlineLabel(
        text = stringResource(R.string.transaction_edit_total),
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
            text = totalAmountDisplay,
            color = amountColor
        )
        val localFocusManager = LocalFocusManager.current
        PgBasicTextField(
            value = totalAmount,
            onValueChange = onTotalAmountChange,
            modifier = Modifier.onFocusChanged {
                onTotalAmountFocusChange(it.isFocused)
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            singleLine = true,
            keyboardActions = KeyboardActions(
                onDone = {
                    localFocusManager.clearFocus()
                }
            ),
            textStyle = MaterialTheme.typography.titleSmall.copy(color = amountColor)
        )
    }
}

@Composable
private fun GeneralSection(
    transactionType: TransactionType,
    selectedAccount: String,
    selectedCategoryType: CategoryType,
    selectedTransferAccount: String,
    transactionDate: String,
    onAccountSectionClick: () -> Unit,
    onCategorySectionClick: () -> Unit,
    onTransferAccountSectionClick: () -> Unit,
    onDateSectionClick: () -> Unit,
) {
    PgHeadlineLabel(
        text = stringResource(R.string.transaction_edit_general),
        modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
    )

    ActionContentCell(
        title = if (transactionType == TransactionType.TRANSFER) {
            stringResource(R.string.transaction_edit_account_from)
        } else {
            stringResource(R.string.transaction_edit_account)
        },
        showDivider = true,
        shape = RoundedCornerShape(
            topStart = MediumRadius,
            topEnd = MediumRadius
        ),
        onClick = onAccountSectionClick,
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PgContentTitle(
                    text = selectedAccount,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaDisabled)
                )
                Spacer(Modifier.width(8.dp))
                PgIcon(
                    imageVector = Icons.Rounded.ChevronRight,
                    tint = LocalContentColor.current.copy(alpha = AlphaDisabled)
                )
            }
        }
    )

    if (transactionType == TransactionType.TRANSFER) {
        ActionContentCell(
            title = stringResource(R.string.transaction_edit_account_to),
            showDivider = true,
            shape = Shapes.None,
            onClick = onTransferAccountSectionClick,
            trailing = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PgContentTitle(
                        text = selectedTransferAccount,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaDisabled)
                    )
                    Spacer(Modifier.width(8.dp))
                    PgIcon(
                        imageVector = Icons.Rounded.ChevronRight,
                        tint = LocalContentColor.current.copy(alpha = AlphaDisabled)
                    )
                }
            }
        )
    }

    if (transactionType == TransactionType.EXPENSE) {
        ActionContentCell(
            title = stringResource(R.string.category),
            showDivider = true,
            shape = Shapes.None,
            onClick = onCategorySectionClick,
            trailing = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val (emoji, name) = selectedCategoryType.getEmojiAndText()
                    PgContentTitle(
                        text = stringResource(name) + " " + emoji,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaDisabled),
                    )
                    Spacer(Modifier.width(8.dp))
                    PgIcon(
                        imageVector = Icons.Rounded.ChevronRight,
                        tint = LocalContentColor.current.copy(alpha = AlphaDisabled)
                    )
                }
            }
        )
    }

    ActionContentCell(
        title = stringResource(R.string.transaction_edit_date_transaction),
        showDivider = false,
        shape = RoundedCornerShape(
            bottomStart = MediumRadius,
            bottomEnd = MediumRadius
        ),
        onClick = onDateSectionClick,
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PgContentTitle(
                    text = transactionDate,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaDisabled)
                )
                Spacer(Modifier.width(8.dp))
                PgIcon(
                    imageVector = Icons.Rounded.ChevronRight,
                    tint = LocalContentColor.current.copy(alpha = AlphaDisabled)
                )
            }
        }
    )
}

@Composable
private fun NoteSection(
    note: TextFieldValue,
    hint: String,
    onNoteChange: (TextFieldValue) -> Unit
) {
    PgHeadlineLabel(
        text = stringResource(R.string.transaction_edit_note),
        modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
    )
    Box(
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.secondary,
            shape = MaterialTheme.shapes.medium
        )
            .fillMaxWidth()
            .paddingCell()
    ) {
        val focusManager = LocalFocusManager.current
        PgBasicTextField(
            value = note,
            onValueChange = onNoteChange,
            keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            placeholderValue = hint
        )
    }
}
