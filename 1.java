Iterator var41 = sunTables.iterator();

                    CardInfo cardInfo;
                    List sonDatas;
                    do {
                        do {
                            Iterator var23;
                            TableInfo sunTable;
                            do {
                                do {
                                    do {
                                        if (!var41.hasNext()) {
                                            var41 = sunTableDatas.keySet().iterator();

                                            while (var41.hasNext()) {
                                                sunTable = (TableInfo) var41.next();
                                                List<Map<String, Object>> sonDatas = sunTableDatas.get(sunTable);
                                                int i = 0;

                                                for (var23 = sonDatas.iterator(); var23.hasNext(); ++i) {
                                                    Map<String, Object> item = (Map) var23.next();
                                                    check = this.dataCheck(this.formDataKit.formatData(formData), i, item, sunTableFields.get(sunTable.getCode()), configFieldMap.get(sunTable.getCode()), serialsMap, nodeId, checkMust);
                                                    if (check.getCode() != 200) {
                                                        return check;
                                                    }
                                                }
                                            }

                                            Map<String, Object> dynamicCardDatas = new HashMap();
                                            if (formData.containsKey("form_dynamic")) {
                                                Optional.ofNullable(formData.getJSONArray("form_dynamic")).orElse(new JSONArray()).forEach((jsonObj) -> {
                                                    Iterator keys = com.alibaba.fastjson.JSONObject.parseObject(com.alibaba.fastjson.JSONObject.toJSONString(jsonObj)).keySet().iterator();

                                                    while (keys.hasNext()) {
                                                        String key = (String) keys.next();
                                                        String value = com.alibaba.fastjson.JSONObject.parseObject(com.alibaba.fastjson.JSONObject.toJSONString(jsonObj)).getString(key);
                                                        dynamicCardDatas.put(key, StrKit.isEmpty(value) ? "" : value);
                                                    }

                                                });
                                                this.formDataKit.dateFormat(dynamicCardDatas);
                                            }

                                            JavaEnhance javaEnhance = this.javaEnhanceService.getJavaEnhance(head.getCode());
                                            FormHandler formHandler;
                                            R r;
                                            if (checkMust) {
                                                try {
                                                    if (javaEnhance != null && StrKit.isNotEmpty(javaEnhance.getSource().trim())) {
                                                        formHandler = this.springGlueFactory.loadFormHandlerInstance(javaEnhance.getSource());
                                                        r = formHandler.beforeUpdate(mainData, sunTableDatasJSON);
                                                        if (r.getCode() != 200) {
                                                            return r;
                                                        }
                                                    }
                                                } catch (Exception var36) {
                                                    var36.printStackTrace();
                                                }
                                            } else {
                                                try {
                                                    if (javaEnhance != null && StrKit.isNotEmpty(javaEnhance.getSource().trim())) {
                                                        formHandler = this.springGlueFactory.loadFormHandlerInstance(javaEnhance.getSource());
                                                        r = formHandler.beforeTempSave(mainData, sunTableDatasJSON);
                                                        if (r.getCode() != 200) {
                                                            return r;
                                                        }
                                                    }
                                                } catch (Exception var35) {
                                                    var35.printStackTrace();
                                                }
                                            }

                                            if (StrKit.isNotEmpty(head.getInsertInterceptorPre()) && lj) {
                                                try {
                                                    Map<String, Object> body = new HashMap();
                                                    body.put("mainData", mainData);
                                                    body.put("sunTableDatas", sunTableDatasJSON);
                                                    HttpResponse res = HttpRequest.post(head.getInsertInterceptorPre()).header("Content-Type", "application/json").header("Authorization", "bearer " + SecurityUtils.getAccessToken()).body(com.alibaba.fastjson.JSONObject.toJSONString(body)).charset("UTF-8").execute().charset("UTF-8");
                                                    if (res.isOk()) {
                                                        JSONObject resObj = JSONUtil.parseObj(res.body());
                                                        if (resObj.getInt("code") != 200) {
                                                            return this.renderError(resObj.getStr("msg"));
                                                        }

                                                        com.alibaba.fastjson.JSONObject resData = com.alibaba.fastjson.JSONObject.parseObject(resObj.getStr("data"));
                                                        if (resData.containsKey("mainData")) {
                                                            Optional.ofNullable(mainFields).orElse(new LinkedList()).forEach((mainField) -> {
                                                                this.formDataKit.setSaveData(resData.getJSONObject("mainData"), mainField, mainData);
                                                                mainData.put("id", resData.getJSONObject("mainData").getString("id"));
                                                                mainData.put("serial", resData.getJSONObject("mainData").getString("serial"));
                                                            });
                                                        }

                                                        if (resData.containsKey("sunTableDatas")) {
                                                            com.alibaba.fastjson.JSONObject sunTableDatasJson = resData.getJSONObject("sunTableDatas");
                                                            Iterator var26 = sunTables.iterator();

                                                            while (var26.hasNext()) {
                                                                TableInfo sunTable = (TableInfo) var26.next();
                                                                if (sunTableDatasJson.containsKey(sunTable.getCode())) {
                                                                    List<Map<String, Object>> sunDatas = new LinkedList();
                                                                    Optional.ofNullable(sunTableDatasJson.getJSONArray(sunTable.getCode())).orElse(new JSONArray()).forEach((jsonObj) -> {
                                                                        Map<String, Object> sunData = new HashMap();
                                                                        com.alibaba.fastjson.JSONObject jsonData = com.alibaba.fastjson.JSONObject.parseObject(com.alibaba.fastjson.JSONObject.toJSONString(jsonObj));
                                                                        Optional.ofNullable(sunTableFields.get(sunTable.getCode())).orElse(new LinkedList()).forEach((sunField) -> {
                                                                            this.formDataKit.setSaveData(jsonData, sunField, sunData);
                                                                        });
                                                                        sunData.put("sort", jsonData.getString("sort"));
                                                                        sunData.put("id", jsonData.getString("id"));
                                                                        sunDatas.add(sunData);
                                                                    });
                                                                    sunTableDatas.put(sunTable, sunDatas);
                                                                }
                                                            }
                                                        }
                                                    }
                                                } catch (Exception var40) {
                                                    log.info("{}保存前置拦截器接口调用失败：{}", head.getCode(), var40.getMessage());
                                                }
                                            }

                                            if (StrKit.isNotEmpty(formData.getString("id"))) {
                                                try {
                                                    MapCRUD oldData = this.service.mapQuery().select("*").eq("id", mainData.get("id")).one(mainTable.getDs(), mainTable.getCode());
                                                    BeanKit.merge(mainData, oldData.getMap());
                                                    SqlKit.injectionData(mainData);
                                                    if (!head.getIsFlow()) {
                                                        if (checkMust) {
                                                            mainData.put("flow_state", 3);
                                                        } else {
                                                            mainData.put("flow_state", 0);
                                                        }
                                                    }

                                                    SqlKit.injectionSerials(this.formulaService, mainTable, mainData, serialsMap);
                                                    this.service.updateById(mainTable.getDs(), mainTable.getCode(), mainData);
                                                    if (sunTableDatas != null && sunTableDatas.size() > 0) {
                                                        Iterator var57 = sunTableDatas.entrySet().iterator();

                                                        while (var57.hasNext()) {
                                                            Entry<TableInfo, List<Map<String, Object>>> tableInfoListEntry = (Entry) var57.next();
                                                            TableInfo k = tableInfoListEntry.getKey();
                                                            List<Map<String, Object>> v = tableInfoListEntry.getValue();
                                                            Map<String, MapCRUD> oldMap = BeanKit.objectByIdMap(this.service.mapQuery().eq("father_id", mainData.get("id")).list(k.getDs(), k.getCode()));
                                                            List<Map<String, Object>> installs = new ArrayList();
                                                            List<Map<String, Object>> updates = new ArrayList();
                                                            Iterator var29 = Optional.ofNullable(v).orElse(new LinkedList()).iterator();

                                                            while (true) {
                                                                while (var29.hasNext()) {
                                                                    Map<String, Object> item = (Map) var29.next();
                                                                    if (StrKit.isNotEmpty(item.get("id")) && oldMap.containsKey(item.get("id"))) {
                                                                        MapCRUD oldsunData = oldMap.get(item.get("id"));
                                                                        item.put("father_id", mainData.get("id"));
                                                                        BeanKit.merge(item, oldsunData);
                                                                        SqlKit.injectionData(item);
                                                                        SqlKit.injectionSerials(this.formulaService, mainTable, mainData, serialsMap);
                                                                        updates.add(item);
                                                                        oldMap.remove(item.get("id"));
                                                                    } else {
                                                                        item.put("father_id", mainData.get("id"));
                                                                        SqlKit.injectionData(item);
                                                                        SqlKit.injectionSerials(this.formulaService, mainTable, mainData, serialsMap);
                                                                        installs.add(item);
                                                                    }
                                                                }

                                                                if (updates.size() > 0) {
                                                                    this.service.updateBatchById(k.getDs(), k.getCode(), updates);
                                                                }

                                                                if (installs.size() > 0) {
                                                                    this.service.saveBatch(k.getDs(), k.getCode(), installs);
                                                                }

                                                                if (oldMap.keySet().size() > 0) {
                                                                    this.service.removeByIds(k.getDs(), k.getCode(), oldMap.keySet());
                                                                }
                                                                break;
                                                            }
                                                        }
                                                    }

                                                    this.saveDynamicData(dynamicCardDatas, mainData);
                                                } catch (Exception var38) {
                                                    var38.printStackTrace();
                                                    return this.renderError(LanguageConfig.getMessage("form_update_fail") + var38.getMessage());
                                                }

                                                try {
                                                    if (javaEnhance != null && StrKit.isNotEmpty(javaEnhance.getSource().trim())) {
                                                        formHandler = this.springGlueFactory.loadFormHandlerInstance(javaEnhance.getSource());
                                                        r = formHandler.afterUpdate(mainData, sunTableDatasJSON);
                                                        if (r.getCode() != 200) {
                                                            return r;
                                                        }
                                                    }
                                                } catch (Exception var32) {
                                                    var32.printStackTrace();
                                                }

                                                this.insertInterceptorsSuf(head, mainData, sunTableDatasJSON);
                                                return this.renderSuccess(mainData.get("id"), LanguageConfig.getMessage("form_update_ok"));
                                            }

                                            try {
                                                SqlKit.injectionData(mainData);
                                                SqlKit.injectionSerials(this.formulaService, mainTable, mainData, serialsMap);
                                                if (!head.getIsFlow() && checkMust) {
                                                    mainData.put("flow_state", 3);
                                                } else {
                                                    mainData.put("flow_state", 0);
                                                }

                                                this.service.save(mainTable.getDs(), mainTable.getCode(), mainData);
                                                Optional.ofNullable(sunTableDatas).orElse(new HashMap()).forEach((kx, vx) -> {
                                                    ((List) Optional.ofNullable(vx).orElse(new LinkedList())).forEach((item) -> {
                                                        item.put("father_id", mainData.get("id"));
                                                        SqlKit.injectionData(item);
                                                        SqlKit.injectionSerials(this.formulaService, mainTable, mainData, serialsMap);
                                                    });
                                                    if (vx.size() > 0) {
                                                        this.service.saveBatch(kx.getDs(), kx.getCode(), vx);
                                                    }

                                                });
                                                this.saveDynamicData(dynamicCardDatas, mainData);
                                            } catch (Exception var39) {
                                                var39.printStackTrace();
                                                return this.renderError(LanguageConfig.getMessage("form_save_fail") + var39.getMessage());
                                            }

                                            if (checkMust) {
                                                try {
                                                    if (javaEnhance != null && StrKit.isNotEmpty(javaEnhance.getSource().trim())) {
                                                        formHandler = this.springGlueFactory.loadFormHandlerInstance(javaEnhance.getSource());
                                                        r = formHandler.afterUpdate(mainData, sunTableDatasJSON);
                                                        if (r.getCode() != 200) {
                                                            return r;
                                                        }
                                                    }
                                                } catch (Exception var34) {
                                                    var34.printStackTrace();
                                                }

                                                this.insertInterceptorsSuf(head, mainData, sunTableDatasJSON);
                                            } else {
                                                try {
                                                    if (javaEnhance != null && StrKit.isNotEmpty(javaEnhance.getSource().trim())) {
                                                        formHandler = this.springGlueFactory.loadFormHandlerInstance(javaEnhance.getSource());
                                                        r = formHandler.afterTempSave(mainData, sunTableDatasJSON);
                                                        if (r.getCode() != 200) {
                                                            return r;
                                                        }
                                                    }
                                                } catch (Exception var33) {
                                                    var33.printStackTrace();
                                                }
                                            }

                                            return this.renderSuccess(mainData.get("id"), LanguageConfig.getMessage("form_save_ok"));
                                        }

                                        sunTable = (TableInfo) var41.next();
                                    } while (!formData.containsKey("nodeId"));
                                } while (!checkMust);

                                cardInfo = this.cardInfoService.getByCode(sunTable.getCode(), head.getCode());
                            } while (cardInfo == null);

                            List<FormConfigControlField> cardlist = this.controlFieldService.getFormConfigControlFieldByNodeIdOrFieldId(nodeId, cardInfo.getId());
                            if (cardlist != null && cardlist.size() > 0) {
                                var23 = cardlist.iterator();

                                while (var23.hasNext()) {
                                    FormConfigControlField configControlField = (FormConfigControlField) var23.next();
                                    if (StrKit.isNotEmpty(configControlField.getConditionEl())) {
                                        try {
                                            if ((Boolean) this.formulaService.execute(configControlField.getConditionEl(), -1, formData)) {
                                                cardInfo.setIsMust(configControlField.getIsMust());
                                            }
                                        } catch (NoSuchMethodException var37) {
                                        }
                                    } else {
                                        cardInfo.setIsMust(configControlField.getIsMust());
                                    }
                                }
                            }

                            sonDatas = sunTableDatas.get(sunTable);
                        } while (!cardInfo.getIsMust());
                    } while (sonDatas != null && !sonDatas.isEmpty());
