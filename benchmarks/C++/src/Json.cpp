#include "Json.h"

using namespace std;

namespace json {

    any Json::benchmark() {
        return (make_shared<JsonPureStringParser>(rapBenchmarkMinified))->parse();
    }

    bool Json::verifyResult(any r) {
        shared_ptr<JsonValue> result = any_cast<shared_ptr<JsonValue>>(r);
        if (!result->isObject()) { 
            return false; 
        }
        shared_ptr<JsonObject> resultObject = result->asObject();
        if (!resultObject->get("head")->isObject()) { 
            return false; 
        }
        if (!resultObject->get("operations")->isArray()) {    
            return false; 
        }
        shared_ptr<JsonArray> resultArray = resultObject->get("operations")->asArray();
        return resultArray->size() == 156;
    }

}