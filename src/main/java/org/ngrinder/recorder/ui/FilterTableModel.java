/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.recorder.ui;

import java.util.List;

import javax.swing.table.AbstractTableModel;


import net.grinder.plugin.http.tcpproxyfilter.ConnectionFilter;
import net.grinder.tools.tcpproxy.EndPoint;
import net.grinder.util.CollectionUtils;

/**
 * TableModel for the host filter table.
 * 
 * This model contains ConnectionFilter instance.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class FilterTableModel extends AbstractTableModel {
	/** UUID. */
	private static final long serialVersionUID = -1221672488662820272L;
	private final ConnectionFilter connectionFilter;

	private String[] columns = { "I", "HOST", "R" };
	private Class<?>[] columnClass = { Boolean.class, String.class, Integer.class };

	/**
	 * Constructor.
	 * 
	 * @param connectionFilter
	 *            connectionFilter to which this model connect to
	 */
	public FilterTableModel(ConnectionFilter connectionFilter) {
		this.connectionFilter = connectionFilter;
	}

	@Override
	public int getRowCount() {
		return connectionFilter.getSize();
	}

	@Override
	public String getColumnName(int column) {
		return columns[column];
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	/**
	 * Removes the row at <code>row</code> from the model. Notification of the row being removed
	 * will be sent to all the listeners.
	 * 
	 * @param row
	 *            the row index of the row to be removed
	 */
	public void removeRow(int row) {
		EndPoint connection = connectionFilter.getConnectionEndPoint(row);
		connectionFilter.remove(connection);
		fireTableRowsDeleted(row, row);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			EndPoint endPoint = connectionFilter.getConnectionEndPoint(rowIndex);
			return !connectionFilter.isFiltered(endPoint);
		case 1:
			return connectionFilter.getConnectionEndPoint(rowIndex).getHost();
		case 2:
			EndPoint endPoint2 = connectionFilter.getConnectionEndPoint(rowIndex);
			return connectionFilter.getEndPointInfo(endPoint2).getCount();
		default:
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnClass[columnIndex];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		EndPoint connection = connectionFilter.getConnectionEndPoint(rowIndex);
		connectionFilter.setFilter(connection, !(Boolean) aValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// Only first column will be clickable.
		return (columnIndex == 0);
	}

	/**
	 * Select/Unselect the given rows. This method will eventually toggle the underlying
	 * connectionFilter instance.
	 * 
	 * @param selectedRows
	 *            rows to be selected
	 * @param selections
	 *            true if selecting. false if not selecting
	 */
	public void setSelection(int[] selectedRows, boolean selections) {
		for (int rowIndex : selectedRows) {
			EndPoint connection = connectionFilter.getConnectionEndPoint(rowIndex);
			connectionFilter.setFilter(connection, !selections);
		}
	}

	/**
	 * Check the update status and fire table data change event to reflect the changes to the front
	 * UI.
	 */
	public void update() {
		if (connectionFilter.isChanged()) {
			fireTableDataChanged();
		}
	}

	/**
	 * Remove the given rows.
	 * 
	 * @param selectedRows
	 *            selected rows.
	 */
	public void removeRows(int[] selectedRows) {
		List<EndPoint> endPoints = CollectionUtils.newArrayList();
		for (int row : selectedRows) {
			endPoints.add(connectionFilter.getConnectionEndPoint(row));
		}
		connectionFilter.remove(endPoints);
		fireTableDataChanged();
	}
}
