package com.squareup.protoparser;

import org.junit.Test;

import static com.squareup.protoparser.ProtoFile.MAX_TAG_VALUE;
import static com.squareup.protoparser.ProtoFile.MIN_TAG_VALUE;
import static com.squareup.protoparser.ProtoFile.isValidTag;
import static com.squareup.protoparser.TestUtils.NO_EXTENSIONS;
import static com.squareup.protoparser.TestUtils.NO_FIELDS;
import static com.squareup.protoparser.TestUtils.NO_ONEOFS;
import static com.squareup.protoparser.TestUtils.NO_OPTIONS;
import static com.squareup.protoparser.TestUtils.NO_RPCS;
import static com.squareup.protoparser.TestUtils.NO_TYPES;
import static org.assertj.core.api.Assertions.assertThat;

public class ProtoFileTest {
  @Test public void tagValueValidation() {
    assertThat(isValidTag(MIN_TAG_VALUE - 1)).isFalse(); // Less than minimum.
    assertThat(isValidTag(MIN_TAG_VALUE)).isTrue();
    assertThat(isValidTag(1234)).isTrue();
    assertThat(isValidTag(19222)).isFalse(); // Reserved range.
    assertThat(isValidTag(2319573)).isTrue();
    assertThat(isValidTag(MAX_TAG_VALUE)).isTrue();
    assertThat(isValidTag(MAX_TAG_VALUE + 1)).isFalse(); // Greater than maximum.
  }

  @Test public void emptyToString() {
    ProtoFile file = ProtoFile.builder("file.proto").build();
    String expected = "// file.proto\n";
    assertThat(file.toString()).isEqualTo(expected);
  }

  @Test public void emptyWithPackageToString() {
    ProtoFile file = ProtoFile.builder("file.proto").setPackageName("example.simple").build();
    String expected = ""
        + "// file.proto\n"
        + "package example.simple;\n";
    assertThat(file.toString()).isEqualTo(expected);
  }

  @Test public void simpleToString() {
    TypeElement element =
        MessageElement.create("Message", "", "", NO_FIELDS, NO_ONEOFS, NO_TYPES, NO_EXTENSIONS,
            NO_OPTIONS);
    ProtoFile file = ProtoFile.builder("file.proto").addType(element).build();
    String expected = ""
        + "// file.proto\n"
        + "\n"
        + "message Message {}\n";
    assertThat(file.toString()).isEqualTo(expected);
  }

  @Test public void simpleWithImportsToString() {
    TypeElement element =
        MessageElement.create("Message", "", "", NO_FIELDS, NO_ONEOFS, NO_TYPES, NO_EXTENSIONS,
            NO_OPTIONS);
    ProtoFile file =
        ProtoFile.builder("file.proto").addDependency("example.other").addType(element).build();
    String expected = ""
        + "// file.proto\n"
        + "\n"
        + "import \"example.other\";\n"
        + "\n"
        + "message Message {}\n";
    assertThat(file.toString()).isEqualTo(expected);
  }

  @Test public void simpleWithPublicImportsToString() {
    TypeElement element =
        MessageElement.create("Message", "", "", NO_FIELDS, NO_ONEOFS, NO_TYPES, NO_EXTENSIONS,
            NO_OPTIONS);
    ProtoFile file = ProtoFile.builder("file.proto")
        .addPublicDependency("example.other")
        .addType(element)
        .build();
    String expected = ""
        + "// file.proto\n"
        + "\n"
        + "import public \"example.other\";\n"
        + "\n"
        + "message Message {}\n";
    assertThat(file.toString()).isEqualTo(expected);
  }

  @Test public void simpleWithBothImportsToString() {
    TypeElement element =
        MessageElement.create("Message", "", "", NO_FIELDS, NO_ONEOFS, NO_TYPES, NO_EXTENSIONS,
            NO_OPTIONS);
    ProtoFile file = ProtoFile.builder("file.proto")
        .addDependency("example.thing")
        .addPublicDependency("example.other")
        .addType(element)
        .build();
    String expected = ""
        + "// file.proto\n"
        + "\n"
        + "import \"example.thing\";\n"
        + "import public \"example.other\";\n"
        + "\n"
        + "message Message {}\n";
    assertThat(file.toString()).isEqualTo(expected);
  }

  @Test public void simpleWithServicesToString() {
    TypeElement element =
        MessageElement.create("Message", "", "", NO_FIELDS, NO_ONEOFS, NO_TYPES, NO_EXTENSIONS,
            NO_OPTIONS);
    ServiceElement service = ServiceElement.create("Service", "", "", NO_OPTIONS, NO_RPCS);
    ProtoFile file = ProtoFile.builder("file.proto").addType(element).addService(service).build();
    String expected = ""
        + "// file.proto\n"
        + "\n"
        + "message Message {}\n"
        + "\n"
        + "service Service {}\n";
    assertThat(file.toString()).isEqualTo(expected);
  }

  @Test public void simpleWithOptionsToString() {
    TypeElement element =
        MessageElement.create("Message", "", "", NO_FIELDS, NO_ONEOFS, NO_TYPES, NO_EXTENSIONS,
            NO_OPTIONS);
    OptionElement option = OptionElement.create("kit", "kat", false);
    ProtoFile file = ProtoFile.builder("file.proto").addOption(option).addType(element).build();
    String expected = ""
        + "// file.proto\n"
        + "\n"
        + "option kit = \"kat\";\n"
        + "\n"
        + "message Message {}\n";
    assertThat(file.toString()).isEqualTo(expected);
  }

  @Test public void simpleWithExtendsToString() {
    TypeElement element =
        MessageElement.create("Message", "", "", NO_FIELDS, NO_ONEOFS, NO_TYPES, NO_EXTENSIONS,
            NO_OPTIONS);
    ExtendElement extend = ExtendElement.create("Extend", "Extend", "", NO_FIELDS);
    ProtoFile file =
        ProtoFile.builder("file.proto").addExtendDeclaration(extend).addType(element).build();
    String expected = ""
        + "// file.proto\n"
        + "\n"
        + "message Message {}\n"
        + "\n"
        + "extend Extend {}\n";
    assertThat(file.toString()).isEqualTo(expected);
  }

  @Test public void multipleEverythingToString() {
    TypeElement element1 =
        MessageElement.create("Message1", "example.simple.Message1", "", NO_FIELDS, NO_ONEOFS,
            NO_TYPES, NO_EXTENSIONS, NO_OPTIONS);
    TypeElement element2 =
        MessageElement.create("Message2", "example.simple.Message2", "", NO_FIELDS, NO_ONEOFS,
            NO_TYPES, NO_EXTENSIONS, NO_OPTIONS);
    ExtendElement extend1 =
        ExtendElement.create("Extend1", "example.simple.Extend1", "", NO_FIELDS);
    ExtendElement extend2 =
        ExtendElement.create("Extend2", "example.simple.Extend2", "", NO_FIELDS);
    OptionElement option1 = OptionElement.create("kit", "kat", false);
    OptionElement option2 = OptionElement.create("foo", "bar", false);
    ServiceElement service1 =
        ServiceElement.create("Service1", "example.simple.Service1", "", NO_OPTIONS, NO_RPCS);
    ServiceElement service2 =
        ServiceElement.create("Service2", "example.simple.Service2", "", NO_OPTIONS, NO_RPCS);
    ProtoFile file = ProtoFile.builder("file.proto")
        .setPackageName("example.simple")
        .addDependency("example.thing")
        .addPublicDependency("example.other")
        .addType(element1)
        .addType(element2)
        .addService(service1)
        .addService(service2)
        .addExtendDeclaration(extend1)
        .addExtendDeclaration(extend2)
        .addOption(option1)
        .addOption(option2)
        .build();
    String expected = ""
        + "// file.proto\n"
        + "package example.simple;\n"
        + "\n"
        + "import \"example.thing\";\n"
        + "import public \"example.other\";\n"
        + "\n"
        + "option kit = \"kat\";\n"
        + "option foo = \"bar\";\n"
        + "\n"
        + "message Message1 {}\n"
        + "message Message2 {}\n"
        + "\n"
        + "extend Extend1 {}\n"
        + "extend Extend2 {}\n"
        + "\n"
        + "service Service1 {}\n"
        + "service Service2 {}\n";
    assertThat(file.toString()).isEqualTo(expected);

    // Re-parse the expected string into a ProtoFile and ensure they're equal.
    ProtoFile parsed = ProtoSchemaParser.parse("file.proto", expected);
    assertThat(parsed).isEqualTo(file);
  }
}
