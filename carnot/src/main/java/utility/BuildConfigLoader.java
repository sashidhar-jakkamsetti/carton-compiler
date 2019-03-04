package utility;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

public class BuildConfigLoader
{
    public File configFile;

    public BuildConfigLoader(String configFileName)
    {
        configFile = new File(configFileName);
    }

    public BuildInfo load() throws Exception
    {
        JAXBContext jaxbContext = JAXBContext.newInstance(BuildInfo.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        BuildInfo bInfo = (BuildInfo)jaxbUnmarshaller.unmarshal(configFile);
        return bInfo;
    }  
}