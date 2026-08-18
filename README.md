# AcresBLE

The AcresBLE SDK is meant to make Bluetooth integration as easy as possible for our
technology partners. This framework includes functionality to initiate funding to/from slots and
tables and electronic player carding to our card reader.

# Structure

AcresBLE SDK is separated into two Device Managers where each of them has responsibility to manage corresponding Acres BLE device. `CardReaderDeviceManager` is used for communication with card reader device for the purpose of inserting and removing player card via BLE. `SlotAndTableDeviceManager` is used for communication with slots and tables.

# Usage

``` kotlin
@HiltViewModel
class SlotAndTableViewModel @Inject 
constructor(private val deviceManager: SlotAndTableDeviceManager) : ViewModel() {

    val state: StateFlow<SlotAndTableReaderState> = deviceManager.slotAndTableStateFlow

    fun fundTable(amount: Int) = viewModelScope.launch { deviceManager.fundTable(amount) }

    fun cancelCashOut() = viewModelScope.launch { deviceManager.cancel() }

    fun cashOutTable() = viewModelScope.launch { deviceManager.cashOut() }

    fun disconnect() = viewModelScope.launch { deviceManager.disconnectDevice() }
}
```

``` kotlin
@HiltViewModel class CardReaderViewModel @Inject
constructor(private val deviceManager: CardReaderDeviceManager) : ViewModel() {

    private val _state = MutableStateFlow(CardReaderScreenState())
    val state: StateFlow<CardReaderScreenState> = _state

    fun insertPlayerCard(selectedTrack: Track, userId: String) = deviceManager.insertPlayerCard(selectedTrack, userId)

    fun disconnect() = viewModelScope.launch { deviceManager.disconnectDevice() }
}
```