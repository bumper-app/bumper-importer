/*
 * Copyright (C) 2015 Mathieu Nayrolles
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bumper.importer.changesets;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author math
 */
public abstract class AbstractChangesetExtractor {

    protected ExecutorService executor;
    protected String scriptsDir;

    public AbstractChangesetExtractor(int poolSize, String scriptsDir) {
        this.executor = Executors.newFixedThreadPool(poolSize);
        this.scriptsDir = scriptsDir;
    }

    public abstract void extractWholeFiles(String command, String ids);

    public abstract void extractDiffs(String command, String ids);

}
