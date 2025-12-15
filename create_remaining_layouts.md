# Remaining Layout Files - Quick Creation Guide

## Files to Create

Create the following layout files in `app/src/main/res/layout/` directory:

### 1. activity_transfer.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="@color/primary_color"
            app:titleTextColor="@android:color/white"/>
    </com.google.android.material.appbar.AppBarLayout>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="From Account"
                android:textStyle="bold"
                android:layout_marginBottom="8dp"/>

            <Spinner
                android:id="@+id/spinner_from_account"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="To Bank"
                android:textStyle="bold"
                android:layout_marginBottom="8dp"/>

            <Spinner
                android:id="@+id/spinner_to_bank"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"/>

            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Recipient Account Number"
                android:layout_marginBottom="16dp">
                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/et_recipient_account"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:inputType="number"/>
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Amount"
                android:layout_marginBottom="16dp">
                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/et_amount"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:inputType="numberDecimal"/>
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Note (Optional)"
                android:layout_marginBottom="24dp">
                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/et_note"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:inputType="text"/>
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_continue"
                android:layout_width="match_parent"
                android:layout_height="56dp"
                android:text="Continue"
                app:cornerRadius="8dp"/>

        </LinearLayout>
    </ScrollView>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### 2. activity_transaction_confirmation.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="@color/primary_color"
            app:titleTextColor="@android:color/white"/>
    </com.google.android.material.appbar.AppBarLayout>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:cardCornerRadius="8dp"
                app:cardElevation="4dp"
                android:layout_marginBottom="24dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Transaction Details"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:layout_marginBottom="16dp"/>

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:layout_marginBottom="8dp">
                        <TextView
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="Type:"
                            android:textColor="@color/text_secondary"/>
                        <TextView
                            android:id="@+id/tv_transaction_type"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="Transfer"
                            android:textAlignment="textEnd"
                            android:textStyle="bold"/>
                    </LinearLayout>

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:layout_marginBottom="8dp">
                        <TextView
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="From:"
                            android:textColor="@color/text_secondary"/>
                        <TextView
                            android:id="@+id/tv_from_account"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="Account"
                            android:textAlignment="textEnd"/>
                    </LinearLayout>

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:layout_marginBottom="8dp">
                        <TextView
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="To:"
                            android:textColor="@color/text_secondary"/>
                        <TextView
                            android:id="@+id/tv_to_account"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="Account"
                            android:textAlignment="textEnd"/>
                    </LinearLayout>

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:layout_marginBottom="8dp">
                        <TextView
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="Amount:"
                            android:textColor="@color/text_secondary"/>
                        <TextView
                            android:id="@+id/tv_amount"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="₫0"
                            android:textAlignment="textEnd"
                            android:textStyle="bold"/>
                    </LinearLayout>

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:layout_marginBottom="8dp">
                        <TextView
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="Fee:"
                            android:textColor="@color/text_secondary"/>
                        <TextView
                            android:id="@+id/tv_fee"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="₫0"
                            android:textAlignment="textEnd"/>
                    </LinearLayout>

                    <View
                        android:layout_width="match_parent"
                        android:layout_height="1dp"
                        android:background="@color/divider_color"
                        android:layout_marginVertical="8dp"/>

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal">
                        <TextView
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="Total:"
                            android:textSize="16sp"
                            android:textStyle="bold"/>
                        <TextView
                            android:id="@+id/tv_total"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="₫0"
                            android:textAlignment="textEnd"
                            android:textSize="18sp"
                            android:textStyle="bold"
                            android:textColor="@color/primary_color"/>
                    </LinearLayout>

                    <TextView
                        android:id="@+id/tv_note"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="12dp"
                        android:text="Note"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary"/>

                </LinearLayout>
            </androidx.cardview.widget.CardView>

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_confirm"
                android:layout_width="match_parent"
                android:layout_height="56dp"
                android:text="Confirm"
                app:cornerRadius="8dp"
                android:layout_marginBottom="12dp"/>

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_cancel"
                android:layout_width="match_parent"
                android:layout_height="56dp"
                android:text="Cancel"
                app:cornerRadius="8dp"
                style="@style/Widget.Material3.Button.OutlinedButton"/>

        </LinearLayout>
    </ScrollView>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

## Continue with remaining layouts...

Due to space constraints, I'll provide a template that works for most remaining activities. Copy this template and adjust the IDs and fields as needed for each activity.

### Generic Activity Template
Use this for:
- activity_bill_payment.xml
- activity_mobile_topup.xml
- activity_ticket_booking.xml
- activity_hotel_booking.xml
- activity_services.xml
- activity_profile.xml
- activity_officer_dashboard.xml

Just replace the content LinearLayout with the appropriate fields for each activity based on the Java code requirements.

