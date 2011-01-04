/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.gwt.ui.client.table;


public class PageableTableModel extends AbstractTableModel implements TableModelListener {
    
    private TableModel model;
    
    private int pageSize;
    
    private int pageIndex;
    
    public PageableTableModel(TableModel model, int pageSize) {
        this.model = model;
        setPageSize(pageSize);
        model.addTableModelListener(this);
    }
    
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException();
        }
        this.pageSize = pageSize;
        update();
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        if (pageSize < 0 || pageIndex >= getPageCount()) {
            throw new IllegalArgumentException();
        }
        this.pageIndex = pageIndex;
        fireTableModelListeners();
    }
    
    public int getPageCount() {
        return model.getRowCount() / pageSize + (model.getRowCount() % pageSize == 0 ? 0 : 1);
    }

    public void firstPage() {
        setPageIndex(0);
    }

    public void lastPage() {
        setPageIndex(getPageCount() - 1);
    }

    public boolean nextPage() {
        if (pageIndex < getPageCount() - 1) {
            setPageIndex(pageIndex + 1);
            return true;
        }
        return false;
    }

    public boolean prevPage() {
        if (pageIndex > 0) {
            setPageIndex(pageIndex - 1);
            return true;
        }
        return false;
    }
    
    public Object getCell(int rowIndex, int colIndex) {
        return model.getCell(getRowIndex(rowIndex), colIndex);
    }

    public Object getRow(int rowIndex) {
        return model.getRow(getRowIndex(rowIndex));
    }

    public int getRowCount() {
        return Math.min(pageSize, model.getRowCount() - pageIndex * pageSize);
    }
    
    public int getColumnCount() {
        return model.getColumnCount();
    }

    protected int getRowIndex(int rowIndex) {
        return pageIndex * pageSize + rowIndex;
    }

    public void modelChanged(TableModel model) {
        update();
    }
    
    private void update() {
        pageIndex = 0;
        fireTableModelListeners();
    }

}
