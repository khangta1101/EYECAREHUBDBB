import os
import re

directory = r"d:\SWP391_BE\EYECAREHUBDBB\src\main\java\com\example\EyeCareHubDB\controller"

def split_camel(text):
    return re.sub(r'(?<!^)(?=[A-Z])', ' ', text)

for filename in os.listdir(directory):
    if filename.endswith("Controller.java"):
        filepath = os.path.join(directory, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        # Extract name
        class_match = re.search(r'public class (\w+)Controller', content)
        if class_match and '@Tag' not in content:
            name_part = class_match.group(1)
            proper_name = split_camel(name_part)

            import_str = "import io.swagger.v3.oas.annotations.tags.Tag;"
            # simple inject import after package
            content = re.sub(r'(package .+;)', r'\1\n\n' + import_str, content, 1)

            # replace @RestController with @Tag(...) + @RestController
            tag_str = f'@Tag(name = "{proper_name}")\n@RestController'
            content = content.replace("@RestController", tag_str, 1)

            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Updated {filename} with Tag: {proper_name}")
