// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package com.google.firebase.gradle.plugins.measurement.apksize

import com.google.firebase.gradle.plugins.measurement.utils.enums.ColumnName
import com.google.firebase.gradle.plugins.measurement.utils.enums.TableName
import com.google.firebase.gradle.plugins.measurement.utils.reports.JsonReport
import com.google.firebase.gradle.plugins.measurement.utils.reports.Table

/** A helper class that generates the APK size measurement JSON report. */
class ApkSizeJsonBuilder {

    // This comes in as a String and goes out as a String, so we might as well keep it a String
    private final String pullRequestNumber
    private final List<Tuple2<Integer, Integer>> sdkSizes

    ApkSizeJsonBuilder(pullRequestNumber) {
        this.pullRequestNumber = pullRequestNumber
        this.sdkSizes = []
    }

    def addApkSize(sdkId, size) {
        sdkSizes.add(new Tuple2(sdkId, size))
    }

    def toJsonString() {
        if (sdkSizes.isEmpty()) {
            throw new IllegalStateException("No sizes were added")
        }

        def pullRequestTable = new Table(tableName: TableName.PULL_REQUESTS,
                columnNames: [ColumnName.PULL_REQUEST_ID],
                replaceMeasurements: [[pullRequestNumber]])
        def apkSizeTable = new Table(tableName: TableName.APK_SIZES,
                columnNames: [ColumnName.PULL_REQUEST_ID, ColumnName.SDK_ID, ColumnName.APK_SIZE],
                replaceMeasurements: sdkSizes.collect {
                    [pullRequestNumber, it.first, it.second]
                })
        def jsonReport = new JsonReport(tables: [pullRequestTable, apkSizeTable])

        return jsonReport.toString()
    }
}
