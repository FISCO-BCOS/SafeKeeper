/**
 * Copyright 2014-2020  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fisco.bcos.key.mgr.data;

import org.fisco.bcos.key.mgr.data.entity.DataListParam;
import org.fisco.bcos.key.mgr.data.entity.TbDataInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * mapper about key escrow.
 */
@Repository
public interface DataMapper {

    Integer addDataRow(TbDataInfo tbAccount);

    TbDataInfo queryData(@Param("dataID") String dataID, @Param("dataSubID") String dataSubID);

    Integer deleteDataRow(@Param("dataID") String dataID, @Param("dataSubID") String dataSubID);

    Integer existOfData(@Param("dataID") String dataID, @Param("dataSubID") String dataSubID);

    Integer countOfData(@Param("param") DataListParam param);

    List<TbDataInfo> listOfData(@Param("param") DataListParam param);
}
