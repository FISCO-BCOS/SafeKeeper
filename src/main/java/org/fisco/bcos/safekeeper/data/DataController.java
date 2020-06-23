
/**
 * Copyright 2014-2020  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.fisco.bcos.safekeeper.data;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import com.fasterxml.jackson.databind.JsonNode;
import org.fisco.bcos.safekeeper.account.AccountService;
import org.fisco.bcos.safekeeper.base.entity.BasePageResponse;
import org.fisco.bcos.safekeeper.base.enums.DataStatus;
import org.fisco.bcos.safekeeper.base.properties.ConstantProperties;
import org.fisco.bcos.safekeeper.base.tools.JacksonUtils;
import org.fisco.bcos.safekeeper.data.entity.DataQueryParam;
import org.fisco.bcos.safekeeper.data.entity.DataRequestInfo;
import org.fisco.bcos.safekeeper.data.entity.TbDataInfo;
import org.fisco.bcos.safekeeper.dataescrow.entity.TbDataEscrowInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.fisco.bcos.safekeeper.account.entity.TbAccountInfo;
import org.fisco.bcos.safekeeper.base.code.ConstantCode;
import org.fisco.bcos.safekeeper.base.controller.BaseController;
import org.fisco.bcos.safekeeper.base.entity.BaseResponse;
import org.fisco.bcos.safekeeper.base.exception.SafeKeeperException;
import org.fisco.bcos.safekeeper.base.tools.SafeKeeperTools;
import org.fisco.bcos.safekeeper.token.TokenService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping(value = "dataVault")
public class DataController extends BaseController {

    @Autowired
    private DataService dataService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private TokenService tokenService;

    /**
     * add raw data.
     */
    @PostMapping(value = "/insert")
    @PreAuthorize(ConstantProperties.HAS_ROLE_VISITOR)
    public BaseResponse addRawData(@RequestBody @Valid DataRequestInfo info, BindingResult result) throws SafeKeeperException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addRawData. startTime:{} dataInfo:{}",
                startTime.toEpochMilli(), JacksonUtils.objToString(info));

        // current
        String currentAccount = getCurrentAccount(request);

        // dataJson to independent data row
        List<TbDataInfo> dataInfoList = dataService.dataJsonToRawDataList(currentAccount, info);
        /*for (int i = 0; i < dataInfoList.size(); i++) {
            TbDataInfo dataInfo = dataInfoList.get(i);
            log.info("addRawData each element. dataInfo: {}", JacksonUtils.objToString(dataInfo));
            // add data row
            dataService.addDataRow(dataInfo);
        }*/
        // add data row
        dataService.addDataBatch(dataInfoList);

        DataQueryParam queryParams = new DataQueryParam(currentAccount, dataInfoList.get(0).getDataID());
        List<TbDataInfo> retDataInfoList = dataService.queryData(queryParams);
        if (retDataInfoList == null) {
            throw new SafeKeeperException(ConstantCode.INSERT_DATA_ERROR);
        }
        else {
            // baseResponse.setData(dataService.rawDataListToDataNode(retDataInfoList));
        }

        log.info("end addRawData. useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JacksonUtils.objToString(baseResponse));
        return baseResponse;
    }

    /**
     * update raw data.
     */
    @PutMapping(value = "/update")
    @PreAuthorize(ConstantProperties.HAS_ROLE_VISITOR)
    public BaseResponse updateRawData(@RequestBody @Valid DataRequestInfo info, BindingResult result)
            throws SafeKeeperException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start updateRawData. startTime:{} dataInfo:{}",
                startTime.toEpochMilli(), JacksonUtils.objToString(info));

        // current
        String currentAccount = getCurrentAccount(request);

        // dataJson to independent data row
        List<TbDataInfo> dataInfoList = dataService.dataJsonToRawDataList(currentAccount, info);
        /*for (int i = 0; i < dataInfoList.size(); i++) {
            TbDataInfo dataInfo = dataInfoList.get(i);
            log.info("updateData each element. dataInfo: {}", JacksonUtils.objToString(dataInfo));
            // update data row
            dataService.updateDataRow(dataInfo);
        }*/
        // add data row
        dataService.updateDataBatch(dataInfoList);

        /*DataQueryParam queryParams = new DataQueryParam(currentAccount, dataInfoList.get(0).getDataID());
        List<TbDataInfo> retDataInfoList = dataService.queryData(queryParams);
        baseResponse.setData(dataService.rawDataListToDataNode(retDataInfoList));*/

        log.info("end updateRawData. useTime:{} result:{}", Duration.between(startTime, Instant.now()).toMillis(),
                JacksonUtils.objToString(baseResponse));
        return baseResponse;
    }

    /**
     * query raw data.
     */
    @GetMapping(value = "/query")
    public BaseResponse queryRawData(@RequestParam(value="dataID") String dataID)
            throws SafeKeeperException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryRawData. startTime: {} dataID:{} ", startTime.toEpochMilli(), dataID);

        if (dataID == null || dataID.equals("")) {
            throw new SafeKeeperException(ConstantCode.PARAM_EXCEPTION);
        }

        // current
        String currentAccount = getCurrentAccount(request);

        // query
        DataQueryParam queryParams = new DataQueryParam(currentAccount, dataID);
        List<TbDataInfo> dataInfoList = dataService.queryData(queryParams);
        if (dataInfoList.size() > 0) {
            baseResponse.setData(dataService.rawDataListToDataNode(dataInfoList));
        } else {
            log.info("data info not exists");
            throw new SafeKeeperException(ConstantCode.DATA_NOT_EXISTS);
        }

        log.info("end queryRawData. useTime:{} result:{}", Duration.between(startTime, Instant.now()).toMillis(),
                JacksonUtils.objToString(baseResponse));
        return baseResponse;
    }

    /**
     * delete raw data.
     */
    @DeleteMapping(value = "/delete")
    @PreAuthorize(ConstantProperties.HAS_ROLE_VISITOR)
    public BaseResponse deleteRawData(@RequestParam(value="dataID") String dataID) throws SafeKeeperException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        String currentAccount = getCurrentAccount(request);
        log.info("start deleteRawData. startTime:{} account: {} dataID:{}",
                startTime.toEpochMilli(), currentAccount, dataID);

        DataQueryParam queryParams = new DataQueryParam(currentAccount, dataID);
        List<TbDataInfo> dataInfoList = dataService.queryData(queryParams);
        for (int i = 0; i < dataInfoList.size(); i++) {
            dataInfoList.get(i).setDataStatus(DataStatus.UNAVAILABLE.getValue());
            dataService.updateDataRow(dataInfoList.get(i));
        }

        log.info("end deleteRawData. useTime:{} result:{}", Duration.between(startTime, Instant.now()).toMillis(),
                JacksonUtils.objToString(baseResponse));
        return baseResponse;
    }

    /**
     * query raw data list.
     */
    @GetMapping(value = "/list")
    @PreAuthorize(ConstantProperties.HAS_ROLE_VISITOR)
    public BasePageResponse listRawData(@RequestParam(value="pageNumber") Integer pageNumber,
                                        @RequestParam(value="pageSize") Integer pageSize) throws SafeKeeperException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start listRawData. startTime:{} pageNumber:{} pageSize:{} ",
                startTime.toEpochMilli(), pageNumber, pageSize);

        String currentAccount = getCurrentAccount(request);

        List<String> dataIDList = dataService.listOfDataID(currentAccount, DataStatus.AVAILABLE.getValue());
        int count = dataIDList.size();
        List<JsonNode> listOfData = new ArrayList<>();
        if (count > 0) {
            Integer start = ((pageNumber-1)*pageSize<0)?0:(pageNumber-1)*pageSize;
            Integer end = (pageNumber*pageSize>count)?count:pageNumber*pageSize;
            for (Integer i = start; i < end; i++) {
                DataQueryParam queryParams = new DataQueryParam(currentAccount, dataIDList.get(i));
                List<TbDataInfo> dataInfoList = dataService.queryData(queryParams);
                listOfData.add(dataService.rawDataListToDataNode(dataInfoList));
            }
        }
        pagesponse.setData(listOfData);
        pagesponse.setTotalCount(count);

        log.info("end listRawData. useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JacksonUtils.objToString(pagesponse));
        return pagesponse;
    }

    /**
     * get total value of unspent token .
     */
    @GetMapping(value = "/wedpr/vcl/getUnspentAmount")
    @PreAuthorize(ConstantProperties.HAS_ROLE_VISITOR)
    public BaseResponse getUnspentAmount() throws SafeKeeperException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        // current
        String currentAccount = getCurrentAccount(request);
        log.info("start wedpr/vcl/getUnspentAmount. account: {} startTime:{} ",
                currentAccount, startTime.toEpochMilli());

        // get total value
        JsonNode dataNode = dataService.getUnspentAmount(currentAccount);
        baseResponse.setData(dataNode);

        log.info("end wedpr/vcl/getUnspentAmount. useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JacksonUtils.objToString(baseResponse));
        return baseResponse;
    }

    /**
     * get total value of spent token .
     */
    @GetMapping(value = "/wedpr/vcl/getSpentAmount")
    @PreAuthorize(ConstantProperties.HAS_ROLE_VISITOR)
    public BaseResponse getSpentAmount() throws SafeKeeperException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        // current
        String currentAccount = getCurrentAccount(request);
        log.info("start wedpr/vcl/getSpentAmount. account: {} startTime:{} ", currentAccount, startTime.toEpochMilli());

        // get total value
        JsonNode dataNode = dataService.getSpentAmount(currentAccount);
        baseResponse.setData(dataNode);

        log.info("end wedpr/vcl/getSpentAmount. useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JacksonUtils.objToString(baseResponse));
        return baseResponse;
    }

    /**
     * get token list.
     */
    @GetMapping(value = "/wedpr/vcl/getCredentialList")
    @PreAuthorize(ConstantProperties.HAS_ROLE_VISITOR)
    public BaseResponse getCredentialList(@RequestParam(value="value") long value) throws SafeKeeperException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        // current
        String currentAccount = getCurrentAccount(request);
        log.info("start wedpr/vcl/getCredentialList. startTime: {} account: {} value: {}",
                startTime.toEpochMilli(), currentAccount, value);

        // get token List
        List<JsonNode> listOfData = dataService.getCredentialList(currentAccount, value);
        baseResponse.setData(listOfData);

        log.info("end wedpr/vcl/getCredentialList. useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JacksonUtils.objToString(baseResponse));
        return baseResponse;
    }

    /**
     * get current account.
     */
    private String getCurrentAccount(HttpServletRequest request) {
        String token = SafeKeeperTools.getToken(request);
        log.debug("getCurrentAccount account:{}", token);
        return tokenService.getValueFromToken(token);
    }

    /**
     * get permission, account in request param need to be yourself or your creator.
     */
    private void checkPermission(String paramAccount, String currentAccount) {
        if (!paramAccount.equals(currentAccount)) {
            TbAccountInfo tbCurAccount = accountService.queryByAccount(paramAccount);
            if (tbCurAccount == null || !currentAccount.equals(tbCurAccount.getCreator())) {
                log.info("lack of access to the data");
                throw new SafeKeeperException(ConstantCode.LACK_ACCESS_DATA);
            }
        }
    }
}
