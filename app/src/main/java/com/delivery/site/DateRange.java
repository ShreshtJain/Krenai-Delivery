package com.delivery.site;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.delivery.R;
import com.squareup.timessquare.CalendarPickerView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.squareup.timessquare.CalendarPickerView.SelectionMode.RANGE;

public class DateRange extends AppCompatActivity {

    private Button dateSelected;
    private Date eventStartDate, eventEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_range);

        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        final CalendarPickerView calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        Date today = new Date();
        calendar.init(today, nextYear.getTime())
                .inMode(RANGE);

        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {

            @Override
            public void onDateUnselected(Date date) {

            }

            @Override
            public void onDateSelected(Date date) {

                final List<Date> dates = calendar.getSelectedDates();
                final int eventEndDateIndex = dates.lastIndexOf(dates);

                if (dates.size() == 1) {
                    dateSelected.setVisibility(View.GONE);
                    eventStartDate = dates.get(0);
                } else {
                    dateSelected.setVisibility(View.VISIBLE);
                    eventEndDate = dates.get(dates.size() - 1);
                }
            }
        });

        dateSelected = (Button) findViewById(R.id.date_selected);
        dateSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventEndDate != null && eventStartDate != null) {
                    Intent intent = new Intent();
                    intent.putExtra("startDate", eventStartDate.toString());
                    intent.putExtra("endDate", eventEndDate.toString());
                    setResult(1, intent);
                    finish();
                } else {
                    Toast.makeText(DateRange.this, "SELECT VALID DATE RANGE", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
