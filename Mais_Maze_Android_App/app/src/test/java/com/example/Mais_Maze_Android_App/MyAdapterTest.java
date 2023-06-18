package com.example.Mais_Maze_Android_App;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class MyAdapterTest {

    private MyAdapter myAdapter;

    @Mock
    private List<DatabaseRow> mockDataList;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        myAdapter = new MyAdapter(mockDataList);
    }

    @Test
    public void onCreateViewHolder() {
        ViewGroup mockParent = mock(ViewGroup.class);
        View mockView = mock(View.class);
        LayoutInflater mockInflater = mock(LayoutInflater.class);
        when(mockParent.getContext()).thenReturn(mockInflater.getContext());
        when(mockInflater.inflate(R.layout.row_layout, mockParent, false)).thenReturn(mockView);

        MyAdapter.ViewHolder viewHolder = myAdapter.onCreateViewHolder(mockParent, 0);

        assertNotNull(viewHolder);
        assertEquals(mockView, viewHolder.itemView);
        assertEquals(R.id.row_text_view, viewHolder.rowTextView.getId());
    }

    @Test
    public void onBindViewHolder() {
        MyAdapter.ViewHolder mockViewHolder = mock(MyAdapter.ViewHolder.class);
        DatabaseRow mockRowData = mock(DatabaseRow.class);
        when(mockDataList.get(0)).thenReturn(mockRowData);
        when(mockRowData.getRowData()).thenReturn("Test Data");

        myAdapter.onBindViewHolder(mockViewHolder, 0);

        verify(mockViewHolder.rowTextView).setText("Test Data");
    }

    @Test
    public void getItemCount() {
        when(mockDataList.size()).thenReturn(5);

        int itemCount = myAdapter.getItemCount();

        assertEquals(5, itemCount);
    }
}
