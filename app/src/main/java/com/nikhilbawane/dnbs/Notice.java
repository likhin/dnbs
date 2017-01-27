/*
 * Copyright (C) 2016 Nikhil Bawane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nikhilbawane.dnbs;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Created by nikhil on 28/6/15.
 */
public class Notice {
    int id;
    String user;
    String title;
    String description;
    String tag;
    int priority;
    String date;

    Notice(int id, String user, String title, String description, String tag, int priority, String date) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.description = description;
        this.tag = tag;
        this.priority = priority;
        this.date = date;
    }

}
