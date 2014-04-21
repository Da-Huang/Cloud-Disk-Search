EMPTY = 
SPACE = $(EMPTY) $(EMPTY)
CLASSPATH = $(subst $(SPACE),:,$(wildcard lib/*.jar))
JCFLAGS = -d bin -cp $(CLASSPATH) -sourcepath src
JAVAC = javac

vpath %.class bin
vpath %.java src

TEXT_TEMPLATE = "\033[36mTEXT\033[0m"
COMMA = ","

#SOURCE_DIR = $(shell find src -name *.java)
SOURCE_FILES = $(shell find src -name *.java)
#SOURCE_FILES = $(foreach dir,$(SOURCE_DIR),$(wildcard $(dir)/*.java))
#CLASSES = $(foreach dir,$(SOURCE_DIR),$(wildcard $(patsubst src%,bin%,$(dir))/*.class))

build: $(SOURCE_FILES:.java=.class)

%.class: %.java
	@echo $(subst TEXT,"Compiling $< ...",$(TEXT_TEMPLATE))
	$(JAVAC) $(JCFLAGS) $<
	@echo $@

clean:
	@echo $(subst TEXT,"Removing $(TARGET)$(COMMA) Object Files$(COMMA) and Dependency Files.",$(TEXT_TEMPLATE))
#	$(RM) -r bin/*
#	@echo $(SOURCE_FILES:.java=.class)
	@echo $(CLASSPATH)
	@echo $(subst TEXT,"Clean.",$(TEXT_TEMPLATE))

.PHONY: clean
