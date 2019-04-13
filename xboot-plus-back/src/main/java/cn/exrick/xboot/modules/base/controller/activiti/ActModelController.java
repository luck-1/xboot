package cn.exrick.xboot.modules.base.controller.activiti;

import cn.exrick.xboot.common.utils.PageUtil;
import cn.exrick.xboot.common.utils.ResultUtil;
import cn.exrick.xboot.common.vo.PageVo;
import cn.exrick.xboot.common.vo.Result;
import cn.exrick.xboot.config.exception.XbootException;
import cn.exrick.xboot.modules.base.entity.activiti.ActModel;
import cn.exrick.xboot.modules.base.entity.activiti.ActProcess;
import cn.exrick.xboot.modules.base.service.activiti.ActModelService;
import cn.exrick.xboot.modules.base.service.activiti.ActProcessService;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * @author Exrickx
 */
@Slf4j
@RestController
@RequestMapping(value = "/xboot/actModel")
@Transactional
public class ActModelController {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private ActModelService actModelService;

    @Autowired
    private ActProcessService actProcessService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ApiOperation(value = "创建新模型")
    public Result<Object> addModel(@ModelAttribute ActModel actModel){

        // 初始化一个空模型
        Model model = repositoryService.newModel();

        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, actModel.getName());
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, actModel.getDescription());
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, model.getVersion());

        model.setName(actModel.getName());
        model.setKey(actModel.getModelKey());
        model.setMetaInfo(modelNode.toString());

        // 保存模型
        repositoryService.saveModel(model);
        String id = model.getId();

        // 完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.replace("stencilset", stencilSetNode);
        try {
            repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            log.error(e.toString());
            return new ResultUtil<Object>().setErrorMsg("添加模型失败");
        }

        // 保存扩展模型至数据库
        actModel.setId(id);
        actModel.setVersion(model.getVersion());
        actModelService.save(actModel);
        return new ResultUtil<Object>().setSuccessMsg("添加模型成功");
    }

    @RequestMapping(value = "/deploy/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "部署发布模型")
    public Result<Object> deploy(@PathVariable String id,
                                 @RequestParam(required = false) String categoryId) {

        // 获取模型
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return new ResultUtil<Object>().setErrorMsg("模型数据为空，请先成功设计流程并保存");
        }

        try {
            JsonNode modelNode = new ObjectMapper().readTree(bytes);

            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            if(model.getProcesses().size()==0){
                return new ResultUtil<Object>().setErrorMsg("模型不符要求，请至少设计一条主线流程");
            }
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

            // 部署发布模型流程
            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .addString(processName, new String(bpmnBytes, "UTF-8"))
                    .deploy();

            // 设置流程分类 保存扩展流程至数据库
            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
            ActModel actModel = actModelService.get(id);
            for (ProcessDefinition pd : list) {
                ActProcess actProcess = new ActProcess();
                if(StrUtil.isNotBlank(categoryId)){
                    repositoryService.setProcessDefinitionCategory(pd.getId(), categoryId);
                    actProcess.setCategoryId(categoryId);
                }
                actProcess.setId(pd.getId());
                actProcess.setName(modelData.getName());
                actProcess.setProcessKey(modelData.getKey());
                actProcess.setDeploymentId(deployment.getId());
                actProcess.setDescription(actModel.getDescription());
                actProcess.setVersion(pd.getVersion());
                actProcess.setXmlName(pd.getResourceName());
                actProcess.setDiagramName(pd.getDiagramResourceName());
                actProcessService.save(actProcess);
            }
        }catch (Exception e){
            log.error(e.toString());
            return new ResultUtil<Object>().setErrorMsg("部署失败");
        }

        return new ResultUtil<Object>().setSuccessMsg("部署成功");
    }

    @RequestMapping(value = "/deployByFile", method = RequestMethod.POST)
    @ApiOperation(value = "通过文件部署模型")
    public Result<Object> deployByFile(@RequestParam MultipartFile file) {

        String fileName = file.getOriginalFilename();
        if (StrUtil.isBlank(fileName)) {
            return new ResultUtil<Object>().setErrorMsg("请先选择文件");
        }
        try {
            InputStream fileInputStream = file.getInputStream();
            Deployment deployment;
            String extension = FilenameUtils.getExtension(fileName);
            String baseName = FilenameUtils.getBaseName(fileName);
            if ("zip".equals(extension) || "bar".equals(extension)) {
                ZipInputStream zip = new ZipInputStream(fileInputStream);
                deployment = repositoryService.createDeployment().name(baseName)
                        .addZipInputStream(zip).deploy();
            } else if ("png".equals(extension)) {
                deployment = repositoryService.createDeployment().name(baseName)
                        .addInputStream(fileName, fileInputStream).deploy();
            } else if (fileName.indexOf("bpmn20.xml") != -1) {
                deployment = repositoryService.createDeployment().name(baseName)
                        .addInputStream(fileName, fileInputStream).deploy();
            } else if ("bpmn".equals(extension)) {
                deployment = repositoryService.createDeployment().name(baseName)
                        .addInputStream(baseName + ".bpmn20.xml", fileInputStream).deploy();
            } else {
                return new ResultUtil<Object>().setErrorMsg("不支持的文件格式");
            }

            // 设置流程分类 保存扩展流程至数据库
            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
            for (ProcessDefinition pd : list) {
                ActProcess actProcess = new ActProcess();
                actProcess.setId(pd.getId());
                actProcess.setName(deployment.getName());
                actProcess.setDeploymentId(deployment.getId());
                actProcess.setProcessKey(pd.getKey());
                actProcess.setVersion(pd.getVersion());
                actProcess.setXmlName(pd.getResourceName());
                actProcess.setDiagramName(pd.getDiagramResourceName());
                actProcessService.save(actProcess);
            }

        } catch (Exception e) {
            log.error(e.toString());
            return new ResultUtil<Object>().setErrorMsg("部署失败");
        }

        return new ResultUtil<Object>().setSuccessMsg("部署成功");
    }

    @RequestMapping(value = "/export/{id}", method = RequestMethod.GET)
    public void export(@PathVariable String id, HttpServletResponse response) {

        try {
            Model modelData = repositoryService.getModel(id);
            // 获取节点信息
            byte[] nodeBytes = repositoryService.getModelEditorSource(modelData.getId());
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = new ObjectMapper().readTree(nodeBytes);
            // 将节点信息转换为xml
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);

            ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);

            String filename = modelData.getName() + ".bpmn20.xml";
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));

            IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e){
            log.error(e.toString());
            throw new XbootException("导出模型出错");
        }
    }

    @RequestMapping(value = "/getByCondition",method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Result<Page<ActModel>> getFileList(@ModelAttribute ActModel actModel,
                                              @ModelAttribute PageVo pageVo){

        Page<ActModel> page = actModelService.findByCondition(actModel, PageUtil.initPage(pageVo));
        return new ResultUtil<Page<ActModel>>().setData(page);
    }

    @RequestMapping(value = "/delByIds/{ids}",method = RequestMethod.DELETE)
    @ApiOperation(value = "通过id批量删除")
    public Result<Object> delByIds(@PathVariable String[] ids){

        for(String id :ids){
            repositoryService.deleteModel(id);
            actModelService.delete(id);
        }
        return new ResultUtil<Object>().setSuccessMsg("删除成功");
    }
}
