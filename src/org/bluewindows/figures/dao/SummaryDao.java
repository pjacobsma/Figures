/**
 * Copyright 2010 Phil Jacobsma
 * 
 * This file is part of Figures.
 *
 * Figures is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Figures is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Figures; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.bluewindows.figures.dao;

import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.SummaryReport;

public interface SummaryDao {

	public CallResult getReports();
	public CallResult saveReport(SummaryReport report);
	public CallResult updateReport(SummaryReport report);
	public CallResult deleteReport(SummaryReport report);
	public CallResult getSummaries(SummaryReport report);

}
