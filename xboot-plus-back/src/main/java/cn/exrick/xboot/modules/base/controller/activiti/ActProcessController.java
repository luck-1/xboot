package cn.exrick.xboot.modules.base.controller.activiti;

import cn.exrick.xboot.common.constant.ActivitiConstant;
import cn.exrick.xboot.common.utils.PageUtil;
import cn.exrick.xboot.common.utils.ResultUtil;
import cn.exrick.xboot.common.vo.PageVo;
import cn.exrick.xboot.common.vo.Result;
import cn.exrick.xboot.common.vo.SearchVo;
import cn.exrick.xboot.config.exception.XbootException;
import cn.exrick.xboot.modules.base.entity.activiti.ActModel;
import cn.exrick.xboot.modules.base.entity.activiti.ActProcess;
import cn.exrick.xboot.modules.base.service.activiti.ActModelService;
import cn.exrick.xboot.modules.base.service.activiti.ActProcessService;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Date;


/**
 * @author Exrick
 */
@Slf4j
@RestController
@Api(description = "流程管理管理接口")
@RequestMapping("/xboot/actProcess")
@Transactional
public class ActProcessController {

    @Autowired
    private ActModelService actModelService;

    @Autowired
    private ActProcessService actProcessService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @RequestMapping(value = "/getByCondition",method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取流程列表")
    public Result<Page<ActProcess>> getByCondition(@ModelAttribute ActProcess actProcess,
                                                   @ModelAttribute SearchVo searchVo,
                                                   @ModelAttribute PageVo pageVo){

        Page<ActProcess> page = actProcessService.findByCondition(actProcess, searchVo, PageUtil.initPage(pageVo));
        return new ResultUtil<Page<ActProcess>>().setData(page);
    }

    @RequestMapping(value = "/updateInfo",method = RequestMethod.POST)
    @ApiOperation(value = "修改所属分类或备注")
    public Result<Object> updateInfo(@RequestParam String id,
                                     @RequestParam String categoryId,
                                     @RequestParam(required = false) String description){

        repositoryService.setProcessDefinitionCategory(id, categoryId);
        ActProcess actProcess = actProcessService.get(id);
        actProcess.setCategoryId(categoryId);
        if(StrUtil.isNotBlank(description)){
            actProcess.setDescription(description);
        }
        actProcessService.update(actProcess);
        return new ResultUtil<Object>().setData("修改成功");
    }

    @RequestMapping(value = "/updateStatus",method = RequestMethod.POST)
    @ApiOperation(value = "修改流程运行状态")
    public Result<Object> updateStatus(@RequestParam String id,
                                       @RequestParam Integer status){

        if(ActivitiConstant.PROCESS_STATUS_ACTIVE.equals(status)){
            repositoryService.activateProcessDefinitionById(id, true, new Date());
        }else if(ActivitiConstant.PROCESS_STATUS_SUSPEND.equals(status)){
            repositoryService.suspendProcessDefinitionById(id, true, new Date());
        }

        ActProcess actProcess = actProcessService.get(id);
        actProcess.setStatus(status);
        actProcessService.update(actProcess);
        return new ResultUtil<Object>().setData("修改成功");
    }

    @RequestMapping(value = "/export",method = RequestMethod.GET)
    @ApiOperation(value = "导出部署流程资源")
    public void exportResource(@RequestParam String id,
                               @RequestParam Integer type,
                               HttpServletResponse response){

        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id).singleResult();

        String resourceName = "";
        if (ActivitiConstant.RESOURCE_TYPE_XML.equals(type)) {
            resourceName = pd.getResourceName();
        } else if (ActivitiConstant.RESOURCE_TYPE_IMAGE.equals(type)) {
            resourceName = pd.getDiagramResourceName();
        }
        InputStream resourceAsStream = repositoryService.getResourceAsStream(pd.getDeploymentId(),
                resourceName);

        try {
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(resourceName, "UTF-8"));
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
        } catch (IOException e) {
            log.error(e.toString());
            throw new XbootException("导出部署流程资源");
        }
    }

    @RequestMapping(value = "/convertToModel/{id}",method = RequestMethod.GET)
    @ApiOperation(value = "转化流程为模型")
    public Result<Object> convertToModel(@PathVariable String id){

        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        InputStream bpmnStream = repositoryService.getResourceAsStream(pd.getDeploymentId(),
                pd.getResourceName());
        ActProcess actProcess = actProcessService.get(id);

        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
            XMLStreamReader xtr = xif.createXMLStreamReader(in);
            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

            BpmnJsonConverter converter = new BpmnJsonConverter();

            ObjectNode modelNode = converter.convertToJson(bpmnModel);
            Model modelData = repositoryService.newModel();
            modelData.setKey(pd.getKey());
            modelData.setName(pd.getResourceName());

            ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, actProcess.getName());
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, modelData.getVersion());
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, actProcess.getDescription());
            modelData.setMetaInfo(modelObjectNode.toString());

            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));

            // 保存扩展模型至数据库
            ActModel actModel = new ActModel();
            actModel.setId(modelData.getId());
            actModel.setName(modelData.getName());
            actModel.setModelKey(modelData.getKey());
            actModel.setDescription(actProcess.getDescription());
            actModel.setVersion(modelData.getVersion());
            actModelService.save(actModel);
        }catch (Exception e){
            log.error(e.toString());
            return new ResultUtil<Object>().setErrorMsg("转化流程为模型失败");
        }
        return new ResultUtil<Object>().setData("修改成功");
    }

    @RequestMapping(value = "/delByIds/{ids}",method = RequestMethod.DELETE)
    @ApiOperation(value = "通过id删除流程")
    public Result<Object> delByIds(@PathVariable String[] ids){

        for(String id : ids){
            ActProcess actProcess = actProcessService.get(id);
            // 级联删除
            repositoryService.deleteDeployment(actProcess.getDeploymentId(), true);
            actProcessService.delete(id);
        }
        return new ResultUtil<Object>().setData("删除成功");
    }

    @RequestMapping(value = "/delInsByIds/{ids}",method = RequestMethod.DELETE)
    @ApiOperation(value = "通过id删除运行中的实例")
    public Result<Object> delInsByIds(@PathVariable String[] ids,
                                      @RequestParam(required = false) String reason){

        for(String id : ids){
            if(StrUtil.isNotBlank(reason)){
                reason = "";
            }
            runtimeService.deleteProcessInstance(id, reason);
            actProcessService.delete(id);
        }
        return new ResultUtil<Object>().setData("删除成功");
    }
}
